#include <WiFi.h>
#include <U8g2lib.h>
#include <I2Cdev.h>
#include <Wire.h>
#include <MPU6050.h>
#include "Pins.h"
#include "Camera.h"
#include "Motor.h"

const char* ssid = "SSID";
const char* password = "PSWD";

WiFiServer server(81);

// OV2640 camera
Camera ov2640;

// MPU6050 accelerator and gyroscope
MPU6050 mpu6050;
int16_t ax, ay, az;
int16_t gx, gy, gz;

Motor motor_L, motor_R;


void setup()
{
	motor_L.initialize(14, 5, 12, 6);
	motor_R.initialize(15, 7, 13, 8);
	motor_L.setPwmDuty(0);
	motor_R.setPwmDuty(0);

	Serial.begin(115200);

	Wire.begin(I2C0_SDA, I2C0_SCL);
	Wire.setClock(400000);

	while (!mpu6050.testConnection());
	mpu6050.initialize();

	ov2640.initialize();

	int n = WiFi.scanNetworks();
	Serial.println("scan done");
	if (n == 0)
	{
		Serial.println("no networks found");
	}
	else
	{
		Serial.print(n);
		Serial.println(" networks found");
		for (int i = 0; i < n; ++i)
		{
			// Print SSID and RSSI for each network found
			Serial.print(i + 1);
			Serial.print(": ");
			Serial.print(WiFi.SSID(i));
			Serial.print(" (");
			Serial.print(WiFi.RSSI(i));
			Serial.print(")");
			Serial.println((WiFi.encryptionType(i) == WIFI_AUTH_OPEN) ? " " : "*");
			delay(10);
		}
	}

	WiFi.begin(ssid, password);

	while (WiFi.status() != WL_CONNECTED)
	{
		delay(500);
		Serial.print(".");
	}
	Serial.println("");
	Serial.println("WiFi connected");

	ov2640.startCameraServer();

	Serial.print("Camera Ready! Use 'http://");
	Serial.print(WiFi.localIP());
	Serial.println("' to connect");

	server.begin();
}


float i = -1;
long heart_beat = 0;
void loop()
{
	//motor_L.setPwmDuty(i += 0.02);
	//if (i > 1)i = -1;
	//motor_R.setPwmDuty(i += 0.02);
	//if (i > 1)i = -1;

	mpu6050.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);
	//Serial.print("a/g:\t");
	//Serial.print(ax); Serial.print("\t");
	//Serial.print(ay); Serial.print("\t");
	//Serial.print(az); Serial.print("\t");
	//Serial.print(gx); Serial.print("\t");
	//Serial.print(gy); Serial.print("\t");
	//Serial.println(gz);

	Serial.println(analogRead(38));

	/*while (true)
	{
		int i = analogRead(38);
		if (i > 2100)
		{
			motor_L.setPwmDuty(-1);
			motor_R.setPwmDuty(-1);
		}
		else if (i < 1800 && i>1000)
		{
			motor_L.setPwmDuty(1);
			motor_R.setPwmDuty(1);
		}
		else
		{
			motor_L.setPwmDuty(0);
			motor_R.setPwmDuty(0);
		}
	}*/

	if (millis() - heart_beat > 100)
	{
		motor_L.setPwmDuty(0);
		motor_R.setPwmDuty(0);
	}


	WiFiClient client = server.available();   // listen for incoming clients
	if (client)
	{
		// if you get a client,
		Serial.println("New Client.");           // print a message out the serial port
		String currentLine = "";                // make a String to hold incoming data from the client
		while (client.connected())
		{            // loop while the client's connected
			if (client.available())
			{             // if there's bytes to read from the client,
				char c = client.read();             // read a byte, then
				//Serial.write(c);                    // print it out the serial monitor
				if (c == '\n')
				{
					String angle_val = getValue(currentLine, ':', 0);
					String power_val = getValue(currentLine, ':', 1);
					int angle = atoi(angle_val.c_str());
					int power = atoi(power_val.c_str());

					if (angle > 0 && power > 30)
					{
						float l = power / 100.f + sin((angle - 90) / 57.3);
						float r = power / 100.f - sin((angle - 90) / 57.3);

						motor_L.setPwmDuty(l);
						motor_R.setPwmDuty(r);

						heart_beat = millis();
					}
					else if (angle < 0 && power > 30)
					{
						float l = -power / 100.f - sin((angle - 90) / 57.3);
						float r = -power / 100.f + sin((angle - 90) / 57.3);

						motor_L.setPwmDuty(l);
						motor_R.setPwmDuty(r);

						heart_beat = millis();
					}
					else
					{
						motor_L.setPwmDuty(0);
						motor_R.setPwmDuty(0);
					}


					Serial.print(angle);
					Serial.print(",");
					Serial.println(power);

					currentLine = "";
				}
				else if (c != '\r')
				{  // if you got anything else but a carriage return character,
					currentLine += c;      // add it to the end of the currentLine
				}
			}
		}
	}

	delay(50);


}


String getValue(String data, char separator, int index)
{
	int found = 0;
	int strIndex[] = { 0, -1 };
	int maxIndex = data.length() - 1;

	for (int i = 0; i <= maxIndex && found <= index; i++)
	{
		if (data.charAt(i) == separator || i == maxIndex)
		{
			found++;
			strIndex[0] = strIndex[1] + 1;
			strIndex[1] = (i == maxIndex) ? i + 1 : i;
		}
	}
	return found > index ? data.substring(strIndex[0], strIndex[1]) : "";
}
