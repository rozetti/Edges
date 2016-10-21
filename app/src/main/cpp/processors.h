#pragma once

#include <memory>
#include <vector>
#include "processor.h"

namespace rz
{
    class processor;

    class processors
    {
        typedef std::vector<std::unique_ptr<rz::processor>> processors_t;
        processors_t m_processors;

    public:

        rz::processor::id_type add(std::unique_ptr<rz::processor> processor);
        rz::processor *get(rz::processor::id_type id) const;
    };

}
