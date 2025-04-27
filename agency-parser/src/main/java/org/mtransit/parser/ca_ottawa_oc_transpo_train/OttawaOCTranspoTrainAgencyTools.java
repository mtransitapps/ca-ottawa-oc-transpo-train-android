package org.mtransit.parser.ca_ottawa_oc_transpo_train;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.provider.OttawaOCTranspoProviderCommons;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.mt.data.MAgency;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// https://www.octranspo.com/en/plan-your-trip/travel-tools/developers/
// https://www.octranspo.com/fr/planifiez/outils-dinformation/developpeurs/
public class OttawaOCTranspoTrainAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new OttawaOCTranspoTrainAgencyTools().start(args);
	}

	@Nullable
	@Override
	public List<Locale> getSupportedLanguages() {
		return LANG_EN_FR;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "OC Transpo";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_LIGHT_RAIL;
	}

	@Override
	public boolean excludeRoute(@NotNull GRoute gRoute) {
		if (gRoute.getRouteType() == MAgency.ROUTE_TYPE_BUS) {
			final String rsn = gRoute.getRouteShortName();
			switch (rsn) {
			case "1": // Confederation Line
			case "2": // Bayview - Greenboro
			case "4": // South Keys - Airport
				return KEEP; // wrongfully classified as bus
			}
		}
		return super.excludeRoute(gRoute);
	}

	@Override
	public boolean defaultRouteIdEnabled() {
		return true;
	}

	@Override
	public boolean useRouteShortNameForRouteId() {
		return true;
	}

	@Override
	public boolean defaultRouteLongNameEnabled() {
		return true;
	}

	@Override
	@Nullable
	public String getRouteIdCleanupRegex() {
		return "\\-\\d+$";
	}

	@Override
	public boolean defaultAgencyColorEnabled() {
		return true;
	}

	private static final String AGENCY_COLOR = "C80D1A";

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = CleanUtils.removeVia(tripHeadsign);
		return OttawaOCTranspoProviderCommons.cleanTripHeadsign(tripHeadsign);
	}

	private static final Pattern ENDS_WITH_DIRECTION = Pattern.compile("(" //
			+ "n\\.|s\\.|e\\.|w\\.|o\\." //
			+ "|" //
			+ "north/nord|south/sud|east/est|west/ouest" //
			+ "|" //
			+ "north / nord|south / sud|east / est|west / ouest" //
			+ ")", Pattern.CASE_INSENSITIVE);

	private static final Pattern O_TRAIN_ = CleanUtils.cleanWords("o-train", "o train");
	private static final String O_TRAIN_REPLACEMENT = CleanUtils.cleanWordsReplacement(EMPTY);

	@NotNull
	private String[] getIgnoredWords() {
		return new String[]{
				"TOH"
		};
	}

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = O_TRAIN_.matcher(gStopName).replaceAll(O_TRAIN_REPLACEMENT);
		gStopName = ENDS_WITH_DIRECTION.matcher(gStopName).replaceAll(EMPTY);
		gStopName = CleanUtils.toLowerCaseUpperCaseWords(Locale.ENGLISH, gStopName, getIgnoredWords());
		gStopName = CleanUtils.fixMcXCase(gStopName);
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	// private static final String EE = "EE";
	// private static final String EO = "EO";
	// private static final String NG = "NG";
	// private static final String NO = "NO";
	// private static final String WA = "WA";
	// private static final String WD = "WD";
	// private static final String WH = "WH";
	// private static final String WI = "WI";
	// private static final String WL = "WL";
	// private static final String PLACE = "place";
	// private static final String RZ = "RZ";
	//
	// @Override
	// public int getStopId(@NotNull GStop gStop) {
	// 	String stopCode = getStopCode(gStop);
	// 	if (!stopCode.isEmpty() && CharUtils.isDigitsOnly(stopCode)) {
	// 		return Integer.parseInt(stopCode); // using stop code as stop ID
	// 	}
	// 	//noinspection deprecation
	// 	final String stopId1 = gStop.getStopId();
	// 	final Matcher matcher = DIGITS.matcher(stopId1);
	// 	if (matcher.find()) {
	// 		final int digits = Integer.parseInt(matcher.group());
	// 		final int stopId;
	// 		if (stopId1.startsWith(EE)) {
	// 			stopId = 100_000;
	// 		} else if (stopId1.startsWith(EO)) {
	// 			stopId = 200_000;
	// 		} else if (stopId1.startsWith(NG)) {
	// 			stopId = 300_000;
	// 		} else if (stopId1.startsWith(NO)) {
	// 			stopId = 400_000;
	// 		} else if (stopId1.startsWith(WA)) {
	// 			stopId = 500_000;
	// 		} else if (stopId1.startsWith(WD)) {
	// 			stopId = 600_000;
	// 		} else if (stopId1.startsWith(WH)) {
	// 			stopId = 700_000;
	// 		} else if (stopId1.startsWith(WI)) {
	// 			stopId = 800_000;
	// 		} else if (stopId1.startsWith(WL)) {
	// 			stopId = 900_000;
	// 		} else if (stopId1.startsWith(PLACE)) {
	// 			stopId = 1_000_000;
	// 		} else if (stopId1.startsWith(RZ)) {
	// 			stopId = 1_100_000;
	// 		} else {
	// 			throw new MTLog.Fatal("Stop doesn't have an ID (start with)! %s!", gStop);
	// 		}
	// 		return stopId + digits;
	// 	}
	// 	throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop);
	// }
}
