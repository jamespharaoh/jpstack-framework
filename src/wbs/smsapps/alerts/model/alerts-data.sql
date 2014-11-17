
-- alerts settings

SELECT object_type_insert (
	'alerts_settings',
	'alerts settings',
	'root',
	1);

SELECT service_type_insert (
	'alerts_settings',
	'alerts',
	'Alerts');

SELECT priv_type_insert (
	'alerts_settings',
	'manage',
	'Full control',
	'Full control of the alerts settings',
	true);

SELECT priv_type_insert (
	'alerts_settings',
	'messages',
	'Message history',
	'View message history for alerts',
	true);

SELECT priv_type_insert (
	'alerts_settings',
	'stats',
	'Message stats',
	'View message stats for alerts',
	true);

-- alerts number

SELECT object_type_insert (
	'alerts_number',
	'alerts number',
	'alerts_settings',
	4);

SELECT event_type_insert (
	'alerts_number_created',
	'%0 added number id %1 to %2');

SELECT event_type_insert (
	'alerts_number_deleted',
	'%0 deleted number id %1 from %2');

SELECT event_type_insert (
	'alerts_number_updated',
	'%0 set %1 for number id %2 of %3 to %4');

-- alerts subject

SELECT object_type_insert (
	'alerts_subject',
	'alerts subject',
	'alerts_settings',
	4);

-- alerts status check

SELECT object_type_insert (
	'alerts_status_check',
	'alerts status check',
	'alerts_settings',
	3);

-- alerts alert

SELECT object_type_insert (
	'alerts_alert',
	'alerts alert',
	'alerts_settings',
	3);