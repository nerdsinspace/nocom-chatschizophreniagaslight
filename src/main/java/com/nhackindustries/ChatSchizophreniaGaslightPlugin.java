/*
 * Copyright (C) 2022 Nerds Inc and/or its affiliates. All rights reserved.
 */

package com.nhackindustries;

import net.futureclient.headless.eventbus.SubscribeEvent;
import net.futureclient.headless.eventbus.events.TickEvent;
import net.futureclient.headless.plugin.Plugin;
import net.minecraft.client.entity.EntityPlayerSP;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Plugin.Metadata(name = "ChatSchizophreniaGaslight", version = "1.0")
public final class ChatSchizophreniaGaslightPlugin implements Plugin {
    private static final UUID XZ_9_UUID = UUID.fromString("2EE704B1-1F48-4065-93E6-A91E8AB6AE19"); // the king
    private static final long SEND_RATE = TimeUnit.MINUTES.toMillis(10L);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final Map<Character, String> HEX_CHARS_TO_MORSE = new HashMap<>();
    private static final SimpleDateFormat EST_DATE_FORMAT = new SimpleDateFormat();
    static {
        // 0-9
        HEX_CHARS_TO_MORSE.put('0', "-----");
        HEX_CHARS_TO_MORSE.put('1', ".----");
        HEX_CHARS_TO_MORSE.put('2', "..---");
        HEX_CHARS_TO_MORSE.put('3', "...--");
        HEX_CHARS_TO_MORSE.put('4', "....-");
        HEX_CHARS_TO_MORSE.put('5', ".....");
        HEX_CHARS_TO_MORSE.put('6', "-....");
        HEX_CHARS_TO_MORSE.put('7', "--...");
        HEX_CHARS_TO_MORSE.put('8', "---..");
        HEX_CHARS_TO_MORSE.put('9', "----.");
        // A-F
        HEX_CHARS_TO_MORSE.put('A', ".-");
        HEX_CHARS_TO_MORSE.put('B', "-...");
        HEX_CHARS_TO_MORSE.put('C', "-.-.");
        HEX_CHARS_TO_MORSE.put('D', "-..");
        HEX_CHARS_TO_MORSE.put('E', ".");
        HEX_CHARS_TO_MORSE.put('F', "..-.");

        EST_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("America/New_York"));
    }

    private long lastSent;

    @Override
    public void onEnable(final PluginContext ctx) {
        ctx.subscribers().register(this);
    }

    @Override
    public void onDisable(final PluginContext ctx) {
        ctx.subscribers().unregister(this);
    }

    @SubscribeEvent
    public void onTick(final TickEvent.Pre event) {
        final EntityPlayerSP player = event.ctx.player;
        if (player != null &&
                player.getUniqueID().equals(XZ_9_UUID)) {
            // lazy return if they are probably in the queue
            if (player.isSpectator() ||
                    Math.abs(player.posX) <= 16 && Math.abs(player.posZ) <= 16) {
                return;
            }

            final long now = System.currentTimeMillis();
            if (now - this.lastSent > SEND_RATE) {
                this.lastSent = now;

                final byte[] bytes = new byte[8];
                SECURE_RANDOM.nextBytes(bytes);
                final String morseEncoded = encodeHexToMorse(bytesToHex(bytes));
                if (morseEncoded != null) {
                    player.sendChatMessage(EST_DATE_FORMAT.format(new Date(now)).concat(morseEncoded));
                }
            }
        }
    }

    private static char[] bytesToHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            final int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return hexChars;
    }

    private static String encodeHexToMorse(final char[] hexChars) {
        String morseEncoded = "";
        for (final char nibble : hexChars) {
            final String converted = HEX_CHARS_TO_MORSE.get(nibble);
            if (converted == null) {
                return null;
            }
            morseEncoded = morseEncoded.concat(converted).concat(" ");
        }
        morseEncoded = morseEncoded.trim();
        return !morseEncoded.isEmpty() ? morseEncoded : null;
    }
}
