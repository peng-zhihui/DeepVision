#ifndef MOTOR_H
#define MOTOR_H

#include <stdint.h>


class Motor
{
public:
	Motor();
	~Motor();

	void initialize(int pin_A, int pwm_channel_A, int pin_B, int pwm_channel_B);
	void setPwmDuty(float duty);

private:

	double frequency = 5000;
	uint8_t resolution_bits = 8;

	int pin_A;
	int pin_B;

	int pwm_channel_A;
	int pwm_channel_B;

};

#endif