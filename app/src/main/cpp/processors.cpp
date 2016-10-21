#include "processors.h"
#include "processor.h"

#include <assert.h>

rz::processor::id_type rz::processors::add(std::unique_ptr<rz::processor> processor)
{
    auto id = static_cast<rz::processor::id_type>(m_processors.size());

    processor->set_id(id);
    m_processors.push_back(std::move(processor));

    return id;
}

rz::processor *rz::processors::get(rz::processor::id_type id) const
{
    auto idx = static_cast<processors_t::size_type>(id);

    if (idx >= m_processors.size())
    {
        return nullptr;
    }

    processor *p = m_processors[idx].get();

    assert(p->get_id() == id);

    return p;
}