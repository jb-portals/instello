"use client";

import { cn } from "@instello/ui/lib/utils";
import * as React from "react";
import {
	Combobox,
	ComboboxContent,
	ComboboxEmpty,
	ComboboxInput,
	ComboboxItem,
	ComboboxList,
} from "./combobox";

const TIME_RE = /^([01]?\d|2[0-3]):([0-5]\d)$/;
const DEFAULT_START_TIME = "06:00";
const DEFAULT_END_TIME = "22:00";
const DEFAULT_STEP_MINUTES = 15;

/** Parse `HH:mm` / `H:mm` into minutes from midnight, or null if invalid. */
export function parseTime(time: string): number | null {
	const match = TIME_RE.exec(time.trim());
	if (!match) return null;
	return Number(match[1]) * 60 + Number(match[2]);
}

/** Format minutes from midnight as zero-padded `HH:mm`. */
export function minutesToTime(totalMinutes: number): string {
	const hours = Math.floor(totalMinutes / 60);
	const minutes = totalMinutes % 60;
	return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}`;
}

/** Normalize typed input to `HH:mm`, or null if invalid. */
export function normalizeTimeInput(raw: string): string | null {
	const minutes = parseTime(raw);
	if (minutes === null) return null;
	return minutesToTime(minutes);
}

/** Locale-friendly display label for an `HH:mm` value. */
export function formatTimeLabel(time: string): string {
	const minutes = parseTime(time);
	if (minutes === null) return time;
	const date = new Date();
	date.setHours(Math.floor(minutes / 60), minutes % 60, 0, 0);
	return date.toLocaleTimeString(undefined, {
		hour: "numeric",
		minute: "2-digit",
	});
}

export function buildTimeSlots(options?: {
	startTime?: string;
	endTime?: string;
	stepMinutes?: number;
	minTime?: string;
}): string[] {
	const startMinutes = parseTime(options?.startTime ?? DEFAULT_START_TIME);
	const endMinutes = parseTime(options?.endTime ?? DEFAULT_END_TIME);
	const step = options?.stepMinutes ?? DEFAULT_STEP_MINUTES;
	const minMinutes = options?.minTime ? parseTime(options.minTime) : null;

	if (
		startMinutes === null ||
		endMinutes === null ||
		step <= 0 ||
		endMinutes < startMinutes
	) {
		return [];
	}

	const slots: string[] = [];
	for (let minutes = startMinutes; minutes <= endMinutes; minutes += step) {
		if (minMinutes !== null && minutes <= minMinutes) continue;
		slots.push(minutesToTime(minutes));
	}
	return slots;
}

function isTimeAllowed(
	time: string,
	options: {
		startTime: string;
		endTime: string;
		minTime?: string;
	},
): boolean {
	const minutes = parseTime(time);
	const startMinutes = parseTime(options.startTime);
	const endMinutes = parseTime(options.endTime);
	if (minutes === null || startMinutes === null || endMinutes === null) {
		return false;
	}
	if (minutes < startMinutes || minutes > endMinutes) {
		return false;
	}
	if (options.minTime) {
		const minMinutes = parseTime(options.minTime);
		if (minMinutes !== null && minutes <= minMinutes) {
			return false;
		}
	}
	return true;
}

export type TimeComboboxProps = {
	value: string;
	onChange: (value: string) => void;
	startTime?: string;
	endTime?: string;
	stepMinutes?: number;
	minTime?: string;
	placeholder?: string;
	disabled?: boolean;
	id?: string;
	className?: string;
	emptyLabel?: string;
};

export function TimeCombobox({
	value,
	onChange,
	startTime = DEFAULT_START_TIME,
	endTime = DEFAULT_END_TIME,
	stepMinutes = DEFAULT_STEP_MINUTES,
	minTime,
	placeholder = "Select time",
	disabled = false,
	id,
	className,
	emptyLabel = "No times available",
}: TimeComboboxProps) {
	const slots = React.useMemo(
		() =>
			buildTimeSlots({
				startTime,
				endTime,
				stepMinutes,
				minTime,
			}),
		[startTime, endTime, stepMinutes, minTime],
	);

	const [inputValue, setInputValue] = React.useState(() =>
		value ? formatTimeLabel(value) : "",
	);

	React.useEffect(() => {
		setInputValue(value ? formatTimeLabel(value) : "");
	}, [value]);

	const range = React.useMemo(
		() => ({ startTime, endTime, minTime }),
		[startTime, endTime, minTime],
	);

	const items = React.useMemo(() => {
		const custom = normalizeTimeInput(inputValue);
		if (custom && isTimeAllowed(custom, range) && !slots.includes(custom)) {
			return [...slots, custom].sort((a, b) => {
				const aMinutes = parseTime(a) ?? 0;
				const bMinutes = parseTime(b) ?? 0;
				return aMinutes - bMinutes;
			});
		}
		return slots;
	}, [slots, inputValue, range]);

	function commitFromInput(raw: string) {
		const trimmed = raw.trim();
		if (!trimmed) {
			onChange("");
			setInputValue("");
			return;
		}

		const normalized = normalizeTimeInput(trimmed);
		if (normalized && isTimeAllowed(normalized, range)) {
			onChange(normalized);
			setInputValue(formatTimeLabel(normalized));
			return;
		}

		setInputValue(value ? formatTimeLabel(value) : "");
	}

	return (
		<Combobox
			items={items}
			value={value || null}
			onValueChange={(next) => {
				if (next == null) {
					onChange("");
					setInputValue("");
					return;
				}
				onChange(next);
				setInputValue(formatTimeLabel(next));
			}}
			inputValue={inputValue}
			onInputValueChange={(next) => {
				setInputValue(next);
			}}
			itemToStringLabel={formatTimeLabel}
			disabled={disabled}
			autoHighlight
			onOpenChange={(open) => {
				if (!open) {
					commitFromInput(inputValue);
				}
			}}
		>
			<ComboboxInput
				id={id}
				placeholder={placeholder}
				disabled={disabled}
				showClear={Boolean(value)}
				className={cn("w-full", className)}
				onBlur={() => {
					commitFromInput(inputValue);
				}}
				onKeyDown={(event) => {
					if (event.key === "Enter") {
						commitFromInput(inputValue);
					}
				}}
			/>
			<ComboboxContent className="w-(--anchor-width)">
				<ComboboxEmpty>
					{slots.length === 0 ? emptyLabel : "No matching times"}
				</ComboboxEmpty>
				<ComboboxList>
					{(item) => (
						<ComboboxItem key={item} value={item}>
							{formatTimeLabel(item)}
						</ComboboxItem>
					)}
				</ComboboxList>
			</ComboboxContent>
		</Combobox>
	);
}
