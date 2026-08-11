"use client";

import { Button } from "@instello/ui/components/button";
import { Calendar } from "@instello/ui/components/calendar";
import {
	Popover,
	PopoverContent,
	PopoverTrigger,
} from "@instello/ui/components/popover";
import { IconCalendarEvent } from "@tabler/icons-react";
import { format, parse } from "date-fns";
import { useState } from "react";

type DatePickerFieldProps = {
	id?: string;
	value: string;
	onChange: (value: string) => void;
	onBlur?: () => void;
	placeholder?: string;
	disabled?: boolean;
};

export function DatePickerField({
	id,
	value,
	onChange,
	onBlur,
	placeholder = "Select date",
	disabled,
}: DatePickerFieldProps) {
	const [open, setOpen] = useState(false);
	const selected = value ? parse(value, "yyyy-MM-dd", new Date()) : undefined;

	return (
		<Popover
			open={open}
			onOpenChange={(next) => {
				setOpen(next);
				if (!next) onBlur?.();
			}}
		>
			<PopoverTrigger
				render={
					<Button
						id={id}
						type="button"
						variant="outline"
						disabled={disabled}
						className="w-full justify-start font-normal"
					/>
				}
			>
				<IconCalendarEvent className="size-4 text-muted-foreground" />
				{selected && !Number.isNaN(selected.getTime())
					? format(selected, "PPP")
					: placeholder}
			</PopoverTrigger>
			<PopoverContent align="start" className="w-auto p-0">
				<Calendar
					mode="single"
					selected={selected}
					onSelect={(date) => {
						if (!date) return;
						onChange(format(date, "yyyy-MM-dd"));
						setOpen(false);
					}}
				/>
			</PopoverContent>
		</Popover>
	);
}
