package models.Playtoo.json;

import java.util.ArrayList;
import java.util.List;

import play.db.jpa.Model;

public class JModel {

	public Long ID;

	public JModel(Model model) {
		if (model != null)
			this.ID = model.id;
	}

	public static <J extends JModel, M extends Model> J from(M m, Class<J> c) {
		if (m == null)
			return null;
		J o = null;
		try {
			o = c.getConstructor(m.getClass()).newInstance(m);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}

	public static <J extends JModel, M extends Model> List<J> fromList(
			List<M> ms, Class<J> c) {
		List<J> js = new ArrayList<J>();
		for (M m : ms) {
			J j = from(m, c);
			if (j != null)
				js.add(j);
		}
		return js;
	}

	public static <J extends JModel, M extends Model> J from(M m, Class<J> c,
			Object param) {
		if (m == null)
			return null;
		J o = null;
		try {
			o = c.getConstructor(m.getClass(), param.getClass()).newInstance(m,
					param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}

	public static <J extends JModel, M extends Model> List<J> fromList(
			List<M> ms, Class<J> c, Object param) {
		List<J> js = new ArrayList<J>();
		for (M m : ms) {
			J j = from(m, c, param);
			if (j != null)
				js.add(j);
		}
		return js;
	}

	public static <J extends JModel, M extends Model> J from(M m, Class<J> c,
			Object param1, Object param2) {
		if (m == null)
			return null;
		J o = null;
		try {
			o = c.getConstructor(m.getClass(), param1.getClass(),
					param2.getClass()).newInstance(m, param1, param2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}

	public static <J extends JModel, M extends Model> List<J> fromList(
			List<M> ms, Class<J> c, Object param1, Object param2) {
		List<J> js = new ArrayList<J>();
		for (M m : ms) {
			J j = from(m, c, param1, param2);
			if (j != null)
				js.add(j);
		}
		return js;
	}

}
