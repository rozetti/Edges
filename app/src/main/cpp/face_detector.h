#pragma once

#include <string>
#include "opencv2/objdetect.hpp"

namespace rz
{
    class face_detector
    {
        cv::CascadeClassifier m_classifier;
        bool m_isOk;
        double m_scaleFactor;
        int m_minNeighbours;
        double m_minObjSize;
        double m_maxObjSize;


    public:

        face_detector() :
                m_isOk(false),
                m_scaleFactor(1.0),
                m_minNeighbours(1),
                m_minObjSize(0.0),
                m_maxObjSize(100000.0)
        {
        }

        void load_cascade(std::string const &path);
        void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects);
    };
}