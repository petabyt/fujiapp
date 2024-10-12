#ifndef OBJECT_H
#define OBJECT_H

#include <camlib.h>

enum PtpSortBy {
	PTP_SORT_BY_OLDEST = 1,
	PTP_SORT_BY_NEWEST,
	PTP_SORT_BY_ALPHA_A_Z,
	PTP_SORT_BY_ALPHA_Z_A,
	PTP_SORT_BY_JPEGS_FIRST,
	PTP_SORT_BY_MOVS_FIRST,
	PTP_SORT_BY_RAWS_FIRST,
};

typedef void ptp_object_found_callback(struct PtpRuntime *r, struct PtpObjectInfo *oi, void *arg);

/// @brief Create an object info downloader service. Designed to be used with multithreaded applications.
/// @param callback Will be called when the service downloads a new object. Used to update the UI.
struct ObjectCache *ptp_create_object_service(int *handles, int length, ptp_object_found_callback *callback, void *arg);

/// @brief Get ObjectInfo from the PTP handle.
/// @returns NULL if not downloaded yet by the service.
struct PtpObjectInfo *ptp_object_service_get(struct PtpRuntime *r, struct ObjectCache *oc, int handle);

/// @brief Use with ptp_object_service_length. Returns object by index.
/// @returns NULL if the device didn't return an object info. (returned InvalidObjectHandle)
/// Will skip any objects that haven't been downloaded yet.
struct PtpObjectInfo *ptp_object_service_get_index(struct PtpRuntime *r, struct ObjectCache *oc, int req_i);

/// @brief Get the current length of the array of downloaded objects
int ptp_object_service_length(struct PtpRuntime *r, struct ObjectCache *oc);

/// @brief Step the object service once. This should normally result in one transaction.
int ptp_object_service_step(struct PtpRuntime *r, struct ObjectCache *oc);

/// @brief Set an object by handle as priority - it will be downloaded on the next ptp_object_service_step call.
/// This can be called multiple times, it will add the objects to a priority queue.
void ptp_object_service_add_priority(struct PtpRuntime *r, struct ObjectCache *oc, int handle);

/// @brief Sort all objects in the list.
void ptp_object_service_sort(struct PtpRuntime *r, struct ObjectCache *oc, enum PtpSortBy s);

#endif
