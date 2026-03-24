export interface CountryTimezoneOption {
  country: string;
  code: string;
  timeZones: string[];
}

export const COUNTRY_TIMEZONE_OPTIONS: CountryTimezoneOption[] = [
  { country: 'Australia', code: 'AU', timeZones: ['Australia/Sydney', 'Australia/Adelaide', 'Australia/Perth'] },
  { country: 'Brazil', code: 'BR', timeZones: ['America/Sao_Paulo', 'America/Manaus'] },
  { country: 'Canada', code: 'CA', timeZones: ['America/Toronto', 'America/Winnipeg', 'America/Vancouver'] },
  { country: 'China', code: 'CN', timeZones: ['Asia/Shanghai'] },
  { country: 'France', code: 'FR', timeZones: ['Europe/Paris'] },
  { country: 'Germany', code: 'DE', timeZones: ['Europe/Berlin'] },
  { country: 'India', code: 'IN', timeZones: ['Asia/Kolkata'] },
  { country: 'Ireland', code: 'IE', timeZones: ['Europe/Dublin'] },
  { country: 'Italy', code: 'IT', timeZones: ['Europe/Rome'] },
  { country: 'Japan', code: 'JP', timeZones: ['Asia/Tokyo'] },
  { country: 'Mexico', code: 'MX', timeZones: ['America/Mexico_City', 'America/Cancun'] },
  { country: 'Netherlands', code: 'NL', timeZones: ['Europe/Amsterdam'] },
  { country: 'New Zealand', code: 'NZ', timeZones: ['Pacific/Auckland'] },
  { country: 'Singapore', code: 'SG', timeZones: ['Asia/Singapore'] },
  { country: 'South Africa', code: 'ZA', timeZones: ['Africa/Johannesburg'] },
  { country: 'Spain', code: 'ES', timeZones: ['Europe/Madrid'] },
  { country: 'Sweden', code: 'SE', timeZones: ['Europe/Stockholm'] },
  { country: 'Switzerland', code: 'CH', timeZones: ['Europe/Zurich'] },
  { country: 'United Arab Emirates', code: 'AE', timeZones: ['Asia/Dubai'] },
  { country: 'United Kingdom', code: 'GB', timeZones: ['Europe/London'] },
  { country: 'United States', code: 'US', timeZones: ['America/New_York', 'America/Chicago', 'America/Denver', 'America/Los_Angeles'] }
];
