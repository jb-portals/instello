"use client";

import { Button } from "@instello/ui/components/button";
import { useMemo, useRef } from "react";
import { cn } from "@/lib/utils";

const SLOT_START_MINUTES = 6 * 60;
const SLOT_END_MINUTES = 22 * 60;
const SLOT_STEP_MINUTES = 15;

function minutesToTime(totalMinutes: number): string {
	const hours = Math.floor(totalMinutes / 60);
	const minutes = totalMinutes % 60;
	return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}`;
}

function timeToMinutes(time: string): number | null {
	const match = /^([01]\d|2[0-3]):([0-5]\d)$/.exec(time);
	if (!match) return null;
	return Number(match[1]) * 60 + Number(match[2]);
}

function formatSlotLabel(time: string): string {
	const minutes = timeToMinutes(time);
	if (minutes === null) return time;
	const date = new Date();
	date.setHours(Math.floor(minutes / 60), minutes % 60, 0, 0);
	return date.toLocaleTimeString(undefined, {
		hour: "numeric",
		minute: "2-digit",
	});
}

export function buildTimeSlots(minTime?: string): string[] {
	const minMinutes = minTime ? timeToMinutes(minTime) : null;
	const slots: string[] = [];

	for (
		let minutes = SLOT_START_MINUTES;
		minutes <= SLOT_END_MINUTES;
		minutes += SLOT_STEP_MINUTES
	) {
		if (minMinutes !== null && minutes <= minMinutes) continue;
		slots.push(minutesToTime(minutes));
	}

	return slots;
}

type TimeSlotPickerProps = {
	value: string;
	onChange: (value: string) => void;
	minTime?: string;
	emptyLabel?: string;
	className?: string;
};

export function TimeSlotPicker({
	value,
	onChange,
	minTime,
	emptyLabel = "No times available",
	className,
}: TimeSlotPickerProps) {
	const slots = useMemo(() => buildTimeSlots(minTime), [minTime]);
	const selectedRef = useRef<HTMLButtonElement | null>(null);

	if (slots.length === 0) {
		return (
			<div
				className={cn(
					"flex h-60 items-center justify-center rounded-lg border border-dashed text-sm text-muted-foreground",
					className,
				)}
			>
				{emptyLabel}
			</div>
		);
	}

	return (
		<div
			className={cn(
				"h-60 overflow-y-auto rounded-lg border border-border p-1",
				className,
			)}
		>
			<ul className="flex flex-col gap-0.5">
				{slots.map((slot) => {
					const selected = value === slot;
					return (
						<li key={slot}>
							<Button
								ref={selected ? selectedRef : undefined}
								type="button"
								variant={selected ? "default" : "ghost"}
								className={cn(
									"h-9 w-full justify-center font-normal",
									!selected && "text-foreground",
								)}
								onClick={() => {
									onChange(slot);
									queueMicrotask(() => {
										selectedRef.current?.scrollIntoView({ block: "nearest" });
									});
								}}
							>
								{formatSlotLabel(slot)}
							</Button>
						</li>
					);
				})}
			</ul>
		</div>
	);
}

export { formatSlotLabel, timeToMinutes };
