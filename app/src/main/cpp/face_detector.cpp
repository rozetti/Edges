#include "face_detector.h"

void rz::face_detector::load_cascade(std::string const &path)
{
    m_isOk = m_classifier.load(path.c_str());
}

void rz::face_detector::detect(const cv::Mat &Image, std::vector<cv::Rect> &objects)
{
    m_classifier.detectMultiScale(Image, objects);//, m_scaleFactor, m_minNeighbours, 0, m_minObjSize, m_maxObjSize);
}


