#ifndef W_TIMER_H
#define W_TIMER_H

#include <iostream>
#include <string>
#include <chrono>
#include "JniUtils.hpp"

class Timer
{
public:

    Timer() : _name("Time elapsed:")
    {
        restart();
    }

    explicit Timer(const std::string &name) : _name(name)
    {
        restart();
    }

    inline void restart()
    {
        _start_time = std::chrono::steady_clock::now();
    }

    // return ms
    inline double elapsed(bool restart = false)
    {
        _end_time = std::chrono::steady_clock::now();
        std::chrono::duration<double> diff = _end_time - _start_time;
        if (restart)
            this->restart();
        return diff.count() * 1000;
    }


    void log( const std::string &tip = "",
             bool unit_ms = true    )
    {
        if (unit_ms)
        {
            if (tip.length() > 0)
                LOGD("-- %s: %.3f ms --\n", tip.c_str(), elapsed());
            else
                LOGD("-- %s: %.3f ms --\n", _name.c_str(), elapsed());
        } else
        {
            if (tip.length() > 0)
                LOGD("-- %s: %.6f s --\n", tip.c_str(), elapsed() / 1000.0);
            else
                LOGD("-- %s: %.6f s --\n", _name.c_str(), elapsed() / 1000.0);
        }
    }


private:
    std::chrono::steady_clock::time_point _start_time;
    std::chrono::steady_clock::time_point _end_time;
    std::string _name;
}; // timer

#endif //W_TIMER_H