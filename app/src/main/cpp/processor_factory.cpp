#include "processor_factory.h"

std::unique_ptr<rz::processor> rz::processor_factory::create_processor(enum filter_type_t type)
{
    return std::unique_ptr<rz::processor>(new rz::processor(type));
}
