package com.dawood.peeng.utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public class SlugUtils {

  private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
  private static final Pattern WHITESPACE = Pattern.compile("[\\s+]");

  public static String buildSlug(String input) {

    if (input == null || input.isBlank())
      return "";

    String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");

    String normalized = Normalizer.normalize(nowhitespace, Form.NFD);

    String slug = NONLATIN.matcher(normalized).replaceAll("");

    slug = slug.replaceAll("-{2,}", "-");

    return slug.toLowerCase(Locale.ENGLISH).replaceAll("^-|-$", "");

  }

  public static String makeUniqueSlug(String input) {
    String slug = buildSlug(input);

    String suffix = UUID.randomUUID().toString().substring(0, 4);

    return slug + "-" + suffix;

  }

}
