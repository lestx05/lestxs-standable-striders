package io.github.lestx05.standablestriders;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StandableStriders implements ModInitializer {
	public static final String MOD_ID = "standable_striders";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Standable Striders initialized");
	}
}

