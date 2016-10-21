#pragma once

#include <stdint.h>
#include <memory>

enum filter_type_t
{
    Greyscale,
    Canny,
    Otsu,
    Face,
    Laplacian,
    Box,
    ScharrX,
    ScharrY,
    ScharrQuad,
    ScharrMax,
    SobelX,
    SobelY,
    SobelQuad,
    SobelMax,
    Sobel,
    Chrominance,
    RenderScriptHighPass,
    RenderScriptLowPass,
    RenderScriptBandPass,
    RenderScriptBandStop
};

enum parameter_type_t
{
    INVALID,
    BLUR_KERNEL_SIZE,
    CONTRAST,
    BRIGHTNESS,
    SOBEL_KERNEL_SIZE,
    CANNY_THRESHOLD1,
    CANNY_THRESHOLD2,
    CANNY_KERNEL_SIZE,
    OTSU_THRESHOLD,
    OTSU_MAX,
    PARAMETER_COUNT
};

namespace cv
{
    class Mat;
}

namespace rz
{
    class processor
    {
    public:
        typedef int id_type;

    private:
        void release();

        int m_height;
        int m_width;
        enum filter_type_t m_type;
        id_type m_id;
        uint8_t *m_frameData;

        cv::Mat *m_temp;
        cv::Mat *m_greyscale;
        cv::Mat *m_scharrX;
        cv::Mat *m_scharrY;
        cv::Mat *m_sobelX;
        cv::Mat *m_sobelY;

        int m_parameters[PARAMETER_COUNT];

    public:
        processor();
        processor(enum filter_type_t type);
        ~processor();

        void set_id(int id) { m_id = id; }
        int get_id() const { return m_id; }

        void setup_processors(int width, int height);
        void set_frame_data(uint8_t *data);
        void process(int type, uint8_t *pixels);
        void set_parameter(int parameter, int value);
        bool get_parameter(int parameter, int &value);
        enum filter_type_t get_type() const { return m_type; }
    };
}

