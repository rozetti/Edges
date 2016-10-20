#include <string>
#include <math.h>
#include <stdint.h>
#include <android/log.h>
#include <vector>

#include "processor.h"
#include "face_detector.h"
#include "opencv2/opencv.hpp"

extern rz::face_detector _face_detector;

#define  LOG_TAG    "processor"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

rz::processor::processor() :
        m_height(0),
        m_width(0),
        m_frameData(nullptr),
        m_greyscale(nullptr),
        m_temp(nullptr),
        m_scharrX(nullptr),
        m_scharrY(nullptr),
        m_sobelX(nullptr),
        m_sobelY(nullptr)
{
    memset(m_parameters, 0, sizeof(m_parameters));
}

rz::processor::~processor()
{
    release();
}

static void release(cv::Mat *&mat)
{
    if (mat)
    {
        delete mat;
        mat = nullptr;
    }
}

void rz::processor::release()
{
    if (m_frameData)
    {
        delete [] m_frameData;
        m_frameData = nullptr;
    }

    ::release(m_greyscale);
    ::release(m_temp);
    ::release(m_scharrX);
    ::release(m_scharrY);
    ::release(m_sobelX);
    ::release(m_sobelY);
}

void rz::processor::setup_processors(int width, int height)
{
    m_width = width;
    m_height = height;

    release();

    auto size = static_cast<int>(1.5 * width * height);
    m_frameData = new uint8_t[size];

    m_temp = new cv::Mat(height, width, CV_8UC1);
    m_greyscale = new cv::Mat(height, width, CV_8UC1);
    m_scharrX = new cv::Mat(height, width, CV_8UC1);
    m_scharrY = new cv::Mat(height, width, CV_8UC1);
    m_sobelX = new cv::Mat(height, width, CV_8UC1);
    m_sobelY = new cv::Mat(height, width, CV_8UC1);
}

void rz::processor::set_frame_data(uint8_t *data)
{
    if (m_frameData)
    {
        memcpy(m_frameData, data, m_width * m_height * 1.5);
    }
}

void rz::processor::process(int type, uint8_t *out_pixels)
{
    cv::Mat output(m_height, m_width, CV_8UC4, out_pixels);

    switch ((enum filter_type_t)type)
    {
        case Greyscale:
        {
            auto blur_kernel_size = m_parameters[BLUR_KERNEL_SIZE];
            auto contrast = (static_cast<double>(m_parameters[CONTRAST]) + 100.0) / 100.0;
            auto brightness = 128.0 * (static_cast<double>(m_parameters[BRIGHTNESS]) / 100.0);

            cv::Mat luminance(m_height, m_width, CV_8UC1, m_frameData);
            cv::blur(luminance, *m_temp, cv::Size(blur_kernel_size, blur_kernel_size));
            m_temp->convertTo(*m_greyscale, -1, contrast, brightness);

            equalizeHist(*m_greyscale, *m_greyscale);
            cv::cvtColor(*m_greyscale, output, cv::COLOR_GRAY2BGRA);
            break;
        }
        case Face:
        {
            cv::cvtColor(*m_greyscale, output, cv::COLOR_GRAY2BGRA);

            std::vector<cv::Rect> rects;
            _face_detector.detect(*m_greyscale, rects);

            if (!rects.empty())
            {
                LOGI("found faces: %d\r\n", rects.size());
                for(auto const &rect : rects)
                {
                    rectangle(output, rect.tl(), rect.br(), cv::Scalar(255,0, 255), 3);
                }
            }
            break;
        }
        case Canny:
        {
            auto threshold1 = static_cast<double>(m_parameters[CANNY_THRESHOLD1]);
            auto threshold2 = static_cast<double>(m_parameters[CANNY_THRESHOLD2]);
            auto kernel_size = m_parameters[CANNY_KERNEL_SIZE] * 2 - 1;

            cv::Canny(*m_greyscale, *m_temp, threshold1, threshold2, kernel_size);
            cv::cvtColor(*m_temp, output, CV_GRAY2BGRA);
            break;
        }
        case Otsu:
        {
            auto threshold = static_cast<double>(m_parameters[OTSU_THRESHOLD]);
            auto max = static_cast<double>(m_parameters[OTSU_MAX]);

            cv::threshold(*m_greyscale, *m_temp, threshold, max, CV_THRESH_BINARY | CV_THRESH_OTSU);
            cv::cvtColor(*m_temp, output, CV_GRAY2BGRA);
            break;
        }
        case Laplacian:
        {
            auto kernel_size = m_parameters[SOBEL_KERNEL_SIZE] * 2 - 1;

            cv::Laplacian(*m_greyscale, *m_temp, -1, kernel_size);
            cv::cvtColor(*m_temp, output, CV_GRAY2BGRA);
            break;
        }
        case Box:
        {
            auto kernel_size = m_parameters[SOBEL_KERNEL_SIZE] * 2 - 1;

            cv::boxFilter(*m_greyscale, *m_temp, -1, cv::Size(kernel_size, kernel_size));
            cv::cvtColor(*m_temp, output, CV_GRAY2BGRA);
            break;
        }
        case ScharrX:
        {
            cv::Scharr(*m_greyscale, *m_scharrX, -1, 1, 0);
            cv::cvtColor(*m_scharrX, output, CV_GRAY2BGRA);
            break;
        }
        case ScharrY:
        {
            cv::Scharr(*m_greyscale, *m_scharrY, -1, 0, 1);
            cv::cvtColor(*m_scharrY, output, CV_GRAY2BGRA);
            break;
        }
        case ScharrQuad:
        {
            cv::absdiff(*m_scharrX, *m_scharrY, *m_temp);
            cv::cvtColor(*m_temp, output, CV_GRAY2BGRA);
            break;
        }
        case ScharrMax:
        {
            cv::max(*m_scharrX, *m_scharrY, *m_temp);
            cv::cvtColor(*m_temp, output, CV_GRAY2BGRA);
            break;
        }
        case SobelX:
        {
            auto kernel_size = m_parameters[SOBEL_KERNEL_SIZE] * 2 - 1;

            cv::Sobel(*m_greyscale, *m_sobelX, -1, 1, 0, kernel_size);
            cv::cvtColor(*m_sobelX, output, CV_GRAY2BGRA);
            break;
        }
        case SobelY:
        {
            auto kernel_size = m_parameters[SOBEL_KERNEL_SIZE] * 2 - 1;

            cv::Sobel(*m_greyscale, *m_sobelY, -1, 0, 1, kernel_size);
            cv::cvtColor(*m_sobelY, output, CV_GRAY2BGRA);
            break;
        }
        case SobelQuad:
        {
            cv::absdiff(*m_sobelX, *m_sobelY, *m_temp);
            cv::cvtColor(*m_temp, output, CV_GRAY2BGRA);
            break;
        }
        case SobelMax:
        {
            cv::max(*m_sobelX, *m_sobelY, *m_temp);
            cv::cvtColor(*m_temp, output, CV_GRAY2BGRA);
            break;
        }
        case Sobel:
        {
            auto kernel_size = m_parameters[SOBEL_KERNEL_SIZE] * 2 - 1;

            cv::Sobel(*m_greyscale, *m_temp, -1, 1, 1, kernel_size);
            cv::cvtColor(*m_temp, output, CV_GRAY2BGRA);
            break;
        }
        case Chrominance:
        {
            cv::Mat chrominance(m_height / 2, m_width / 2, CV_16UC1, m_frameData + m_width * m_height);
            chrominance.convertTo(chrominance, CV_8U, 1.0/256.0);
            cv::resize(chrominance, *m_temp, cv::Size(), 2, 2, CV_INTER_LINEAR);
            cv::cvtColor(*m_temp, output, CV_GRAY2BGRA);
            break;
        }
        case RenderScriptHighPass:
        case RenderScriptLowPass:
        case RenderScriptBandPass:
        case RenderScriptBandStop:
        {
            cv::cvtColor(*m_greyscale, output, CV_GRAY2BGRA);
            break;
        }
    }
}

static bool check_parameter(int parameter)
{
    if (parameter >= PARAMETER_COUNT)
    {
        LOGE("parameter %d invalid (>= PARAMETER_COUNT = %d", parameter, PARAMETER_COUNT);
        return false;
    }

    if (parameter <= INVALID)
    {
        LOGE("parameter %d invalid (<= INVALID = %d", parameter, INVALID);
        return false;
    }

    return true;
}

bool rz::processor::get_parameter(int parameter, int &value)
{
    if (!::check_parameter(parameter)) return false;

    value = m_parameters[parameter];

    return true;
}

void rz::processor::set_parameter(int parameter, int value)
{
    if (!::check_parameter(parameter)) return;

    LOGI("SetParameter %d: %d -> %d", parameter, m_parameters[parameter], value);

    m_parameters[parameter] = value;
}
