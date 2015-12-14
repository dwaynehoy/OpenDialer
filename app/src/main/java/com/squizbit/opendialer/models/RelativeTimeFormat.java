package com.squizbit.opendialer.models;

import org.ocpsoft.pretty.time.BasicTimeFormat;
import org.ocpsoft.pretty.time.PrettyTime;
import org.ocpsoft.pretty.time.TimeFormat;
import org.ocpsoft.pretty.time.TimeUnit;
import org.ocpsoft.pretty.time.units.Day;
import org.ocpsoft.pretty.time.units.Hour;
import org.ocpsoft.pretty.time.units.Minute;
import org.ocpsoft.pretty.time.units.Second;
import org.ocpsoft.pretty.time.units.Year;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An extended PrettyTime class which formats the time based on the Timeline formatting rules
 */
public class RelativeTimeFormat extends PrettyTime {

    /**
     * Creates a new PrettyTime Timeline relative time formatter
     * @param date The start date to base the relative date times from
     */
	public RelativeTimeFormat(Date date) {
		super(date);
		Locale locale = getLocale();
		List<TimeUnit> timeUnitList = new ArrayList<TimeUnit>();
		
		timeUnitList.add(new CallLogSecondTimeUnit(locale));
		timeUnitList.add(new CallLogMinuteTimeUnit(locale));
		timeUnitList.add(new CallLogHourTimeUnit(locale));
		timeUnitList.add(new CallLogDayTimeUnit(locale));
		timeUnitList.add(new CallLogYearTimeUnit(locale));
		setUnits(timeUnitList);
	}

	/**
	 * A PrettyTime timeunit that formats the seconds unit as required by the Timeline
	 */
	class CallLogSecondTimeUnit extends Second {

		private final TimeFormat DEFAULT_FORMAT = (new BasicTimeFormat()).setPattern("%n %u").setPastSuffix(" ago").setFutureSuffix(" from now");

		/**
		 * Creates a Timeline unit that displays time in seconds
		 * @param locale The current user locale
		 */
		public CallLogSecondTimeUnit(Locale locale) {
			super(locale);
		}


		@Override
		public TimeFormat getFormat() {
			return DEFAULT_FORMAT;
		}

		@Override
		public String getName() {
			return "Second";
		}

		@Override
		public String getPluralName() {
			return "Seconds";
		}
	}

	/**
	 * A PrettyTime timeunit that formats the minute unit as required by the Timeline
	 */
	class CallLogMinuteTimeUnit extends Minute {

		private final TimeFormat DEFAULT_FORMAT = (new BasicTimeFormat()).setPattern("%n %u").setPastSuffix(" ago").setFutureSuffix(" from now");

		/**
		 * Creates a Timeline unit that displays time in minutes
		 * @param locale The current user locale
		 */
		public CallLogMinuteTimeUnit(Locale locale) {
			super(locale);
		}

		@Override
		public TimeFormat getFormat() {
			return DEFAULT_FORMAT;
		}

		@Override
		public String getName() {
			return "Minute";
		}

		@Override
		public String getPluralName() {
			return "Minutes";
		}

	}

	/**
	 * A PrettyTime timeunit that formats the hour unit as required by the Timeline
	 */
	class CallLogHourTimeUnit extends Hour {

		private final TimeFormat DEFAULT_FORMAT = (new BasicTimeFormat()).setPattern("%n %u").setPastSuffix(" ago").setFutureSuffix(" from now");

		/**
		 * Creates a Timeline unit that displays time in hours
		 * @param locale The current user locale
		 */
		public CallLogHourTimeUnit(Locale locale) {
			super(locale);
		}

		@Override
		public TimeFormat getFormat() {
			return DEFAULT_FORMAT;
		}

		@Override
		public String getName() {
			return "Hour";
		}

		@Override
		public String getPluralName() {
			return "Hours";
		}

	}

	/**
	 * A PrettyTime timeunit that formats the day unit as required by the Timeline
	 */
	class CallLogDayTimeUnit extends Day {

		private final TimeFormat DEFAULT_FORMAT = (new BasicTimeFormat()).setPattern("%n %u").setPastSuffix(" ago").setFutureSuffix(" from now");

		/**
		 * Creates a Timeline unit that displays time in days
		 * @param locale The current user locale
		 */
		public CallLogDayTimeUnit(Locale locale) {
			super(locale);
		}

		@Override
		public TimeFormat getFormat() {
			return DEFAULT_FORMAT;
		}

		@Override
		public String getName() {
			return "Day";
		}

		@Override
		public String getPluralName() {
			return "Days";
		}
	}

	/**
	 * A PrettyTime timeunit that formats the year unit as required by the Timeline
	 */
	class CallLogYearTimeUnit extends Year {

		private final TimeFormat DEFAULT_FORMAT = (new BasicTimeFormat()).setPattern("%n %u").setPastSuffix(" ago").setFutureSuffix(" from now");

		/**
		 * Creates a Timeline unit that displays time in years
		 * @param locale The current user locale
		 */
		public CallLogYearTimeUnit(Locale locale) {
			super(locale);
		}

		@Override
		public TimeFormat getFormat() {
			return DEFAULT_FORMAT;
		}

		@Override
		public String getName() {
			return "Years";
		}

		@Override
		public String getPluralName() {
			return "Years";
		}

	}
}
