export interface Settings {
	height: number;
	width: number;
	theme: Theme
}

export interface Theme {
	main: string;
	secondary: string;
	tertiary: string;
	text_main: string;
	text_secondary: string;
	text_disabled: string;
}
