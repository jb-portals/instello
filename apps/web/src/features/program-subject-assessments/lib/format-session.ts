export function formatSessionDate(sessionDate: string) {
	const [year, month, day] = sessionDate.split("-").map(Number);
	if (!year || !month || !day) return sessionDate;
	return new Date(Date.UTC(year, month - 1, day)).toLocaleDateString(
		undefined,
		{
			year: "numeric",
			month: "short",
			day: "numeric",
			timeZone: "UTC",
		},
	);
}

export function formatSessionTime(time: string) {
	const [hours, minutes] = time.split(":").map(Number);
	if (hours === undefined || minutes === undefined) return time;
	const date = new Date();
	date.setHours(hours, minutes, 0, 0);
	return date.toLocaleTimeString(undefined, {
		hour: "numeric",
		minute: "2-digit",
	});
}

export function formatSessionWindow(
	sessionDate: string,
	sessionStartTime: string,
	sessionEndTime: string,
) {
	return `${formatSessionDate(sessionDate)} · ${formatSessionTime(sessionStartTime)}–${formatSessionTime(sessionEndTime)}`;
}
