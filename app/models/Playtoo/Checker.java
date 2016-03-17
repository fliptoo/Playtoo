package models.Playtoo;

import java.util.List;

import models.Playtoo.Faulty.Type;

import org.apache.commons.lang.StringUtils;

import play.classloading.enhancers.LocalvariablesNamesEnhancer;
import play.i18n.Messages;

public class Checker {

	private static String findName(Object o) {
		String name = null;
		List<String> names = LocalvariablesNamesEnhancer.LocalVariablesNamesTracer
				.getAllLocalVariableNames(o);
		if (names.size() > 0)
			name = names.get(0);
		return name;
	}

	public static void Required(Object obj) throws Faulty {
		Type type = Type.REQUIRED;
		String ref = findName(obj);
		if ((obj instanceof String && StringUtils
				.isEmpty(((String) obj).trim())) || obj == null)
			throw new Faulty(type, ref, Messages.get(type, ref));
	}

	public static void NotFound(Object obj) throws Faulty {
		Type type = Type.NOT_FOUND;
		String ref = findName(obj);
		if (obj == null)
			throw new Faulty(type, ref, Messages.get(type, ref));
	}

	public static void Duplicated(Object obj) throws Faulty {
		Type type = Type.DUPLICATED;
		String ref = findName(obj);
		throw new Faulty(type, ref, Messages.get(type, obj));
	}

	public static void Invalid(Object obj) throws Faulty {
		Type type = Type.INVALID;
		String ref = findName(obj);
		throw new Faulty(type, ref, Messages.get(type, obj));
	}

	public static void Minimum(String ref, double value, double min)
			throws Faulty {
		Type type = Type.MIN;
		if (value < min)
			throw new Faulty(type, ref,
					Messages.get(type, findName(value), min));
	}

	public static void Maximum(String ref, double value, double max)
			throws Faulty {
		Type type = Type.MAX;
		if (value > max)
			throw new Faulty(type, ref,
					Messages.get(type, findName(value), max));
	}

}
