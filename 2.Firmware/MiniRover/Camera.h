#ifndef CAMERA_H
#define CAMERA_H

#include <Arduino.h>
#include "Pins.h"
#include "esp_camera.h"
#include "esp_http_server.h"
#include "esp_timer.h"
#include "img_converters.h"
#include "index_page.h"
#include "fb_gfx.h"
#include "dl_lib_matrix3d.h"

class Camera
{
public:
	Camera();
	~Camera();

	void initialize();
	void startCameraServer();

private:

};


// Web APP stuffs
typedef struct
{
	size_t size; //number of values used for filtering
	size_t index; //current value index
	size_t count; //value count
	int sum;
	int* values; //array to be filled with values
} ra_filter_t;

typedef struct
{
	httpd_req_t* req;
	size_t len;
} jpg_chunking_t;

static ra_filter_t* ra_filter_init(ra_filter_t* filter, size_t sample_size);

static int ra_filter_run(ra_filter_t* filter, int value);

static esp_err_t stream_handler(httpd_req_t* req);

static esp_err_t cmd_handler(httpd_req_t* req);

static esp_err_t status_handler(httpd_req_t* req);

static esp_err_t index_handler(httpd_req_t* req);

static void startCameraServer_impl();

#endif  