#pragma once

#include "processor.h"

namespace rz
{
    class processor_factory
    {
    public:

        std::unique_ptr<rz::processor> create_processor(enum filter_type_t type);
    };
}
