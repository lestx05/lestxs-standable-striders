package com.lestxs.standable.striders;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LestxsStandableStriders implements ModInitializer {
	public static final String MOD_ID = "lestxs-standable-striders";
	public static final String MOD_NAME = "lestx's Standable Striders";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initialized {}", MOD_NAME);
	}
}
