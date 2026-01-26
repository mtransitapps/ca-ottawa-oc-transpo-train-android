package org.mtransit.parser.ca_ottawa_oc_transpo_train;

import static org.mtransit.commons.StringUtils.EMPTY;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CharUtils;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.provider.OttawaOCTranspoProviderCommons;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
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
	public @Nullable String getServiceIdCleanupRegex() {
		return "^[A-Z]+\\d{2}\\-"; // starts with "MMMYY" (JAN26 or SEPT25)
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
		return "\\-\\d+(\\-\\d+)?$";
	}

	@Override
	public boolean verifyRouteIdsUniqueness() {
		return false; // merge routes
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

	@Override
	public @Nullable String getTripIdCleanupRegex() {
		return "^[A-Z]+\\d{2}\\-"; // starts with "MMMYY" (JAN26 or SEPT25)
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
		return CleanUtils.cleanLabel(getFirstLanguageNN(), gStopName);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		String stopCode = getStopCode(gStop);
		if (!stopCode.isEmpty() && CharUtils.isDigitsOnly(stopCode)) {
			return Integer.parseInt(stopCode); // using stop code as stop ID
		}
		throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop.toStringPlus(true));
	}
}
