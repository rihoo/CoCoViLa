package ee.ioc.cs.vsle.synthesize;

import ee.ioc.cs.vsle.vclass.ClassField;
import ee.ioc.cs.vsle.util.db;

import java.io.Serializable;
import java.util.*;
import java.util.regex.*;

class Rel implements Cloneable,
	Serializable {

	List outputs = new ArrayList(); //Collections.synchronizedList(new ArrayList());
	List inputs = new ArrayList(); //Collections.synchronizedList(new ArrayList());
	List subtasks = new ArrayList(); //Collections.synchronizedList(new ArrayList());
	ArrayList exceptions = new ArrayList();
//	static int auxVarCounter = 0;
	List algorithm = new ArrayList(); //Collections.synchronizedList(new ArrayList()); //only for subtasks
	Rel parentRel = null; //parent axiom containing this subtask

	int unknownInputs;
	int subtaskFlag;
	String object;
	String method;
	boolean inAlgorithm = false;

	int type; /* 1 - equals, 2 - method*/

	void setParentRel(Rel rel) {
		if (type != RelType.subtask)
			throw new IllegalStateException(
				"Only subtasks can contain parent rels");

		parentRel = rel;
	}

	Rel getParentRel() {
		if (type != RelType.subtask)
			throw new IllegalStateException(
				"Only subtasks can contain parent rels");

		return parentRel;
	}

	void addRelToAlgorithm(Rel rel) {
		if (type != RelType.subtask)
			throw new IllegalStateException(
				"Only subtasks can contain algorithms");

		algorithm.add(rel);
	}

	List getAlgorithm() {
		if (type != RelType.subtask)
			throw new IllegalStateException(
				"Only subtasks can contain algorithms");

		return algorithm;
	}

	List getOutputs() {
		return outputs;
	}

	List getInputs() {
		return inputs;
	}

	List getSubtasks() {
		return subtasks;
	}

	void setUnknownInputs(int f) {
		unknownInputs = f;
	}

	void setSubtaskFlag(int f) {
		subtaskFlag = f;
	}

	void setObj(String s) {
		object = s;
	}

	void setMethod(String m) {
		method = m;
	}

	void setType(int t) {
		type = t;
		if (type == RelType.alias) {
			aliasNr = RelType.auxVarCounter++;
		}
	}

	private int aliasNr = 0;

	String getMaxType(List inputs) {
		Var var;

		for (int i = 0; i < inputs.size(); i++) {
			var = (Var) inputs.get(i);
			if (!var.type.equals("int")) {
				return "double";
			}
		}
		return "int";
	}

	String getParameters(boolean useBrackets) {
		String params = "";
		if (useBrackets)
			params += "(";
		Var var;
		int j = 0;

		for (int i = 0; i < inputs.size(); i++) {
			var = (Var) inputs.get(i);
			if (!var.type.equals("void")) {
				if (j == 0) {
					params += var;
				} else {
					params += ", " + var;
				}
				j++;
			}
		}
		if (useBrackets)
			return params += ")";
		return params;

	}

	String getSubtaskParameters() {
		String params = "(";
		boolean subExist = false;
		for (int i = 0; i < subtasks.size(); i++) {
			if (i == 0) {
				params += RelType.TAG_SUBTASK;
				subExist = true;
			} else {
				params += ", " + RelType.TAG_SUBTASK;
			}
		}
		for (int i = 0; i < inputs.size(); i++) {
			if (i == 0 && !subExist) {
				params += (Var) inputs.get(i);
			} else {
				params += ", " + (Var) inputs.get(i);
			}
		}
		return params += ")";
	}

	void addInput(Var var) {
		inputs.add(var);
	}

	void addOutput(Var var) {
		outputs.add(var);
	}

	void addSubtask(Rel rel) {
		subtasks.add(rel);
	}

	String getObject(String obj) {
		if (obj.equals("this")) {
			return "";
		} else if (obj.startsWith("this")) {
			return obj.substring(5) + ".";
		} else {
			return obj + ".";
		}
	}

	public String toString() {
		Pattern pattern;
		Matcher matcher;

		if (type == 4) {
			return "";
		}
		if (type == 2) {
			Var op = (Var) outputs.get(0);
			Var input;
			String assigns = "";
			for (int i = 0; i < inputs.size(); i++) {
				input = (Var) inputs.get(i);
				if (input.field.isAlias()) {
					if (input.field.vars.size() == 0) {
						assigns = "Object " + input.name + " = null";
					} else {

						assigns += ((ClassField) input.field.vars.get(0)).type + "[] " + input.name +
							" = new " +
							((ClassField) input.field.vars.get(0)).type + "[" + input.field.vars.size() +
							"];\n";
						for (int k = 0; k < input.field.vars.size(); k++) {
							String s1 = ((ClassField) input.field.vars.get(k)).toString();
							assigns +=  CodeGenerator.OT_TAB+CodeGenerator.OT_TAB+ input.name +
								"[" +
								Integer.toString(k) + "] = " +
								s1 +
								";\n";
						}
						assigns += CodeGenerator.OT_TAB +CodeGenerator.OT_TAB;
					}
				}
			}
			if (op.type.equals("void")) {
				return (getObject(object) + method + getParameters(true));
			} else {
				return assigns + ((Var) outputs.get(0) + " = " + getObject(object) +
					method +
					getParameters(true));
			}
		} else if (type == 3) {
			// if its an array assingment
			if (inputs.size() == 0 && outputs.size() == 1) {
				String assign;
				Var op = (Var) outputs.get(0);

				if (op.field.isPrimOrStringArray()) {
					String[] split = method.split("=");
					assign = op.field.type + " " + " TEMP" +
						Integer.toString(RelType.auxVarCounter) + "=" +
						split[1] +
						";\n";
					assign += getObject(op.object) + op.name + " = TEMP" +
						Integer.toString(RelType.auxVarCounter) + ";\n";
					RelType.auxVarCounter++;
					return assign;

				}
			}

			if (inputs.size() == 1 && outputs.size() == 1) {
				String s1, assigns = "";
				Var ip = (Var) inputs.get(0);
				Var op = (Var) outputs.get(0);

				if (ip.field.isArray() && op.field.isAlias()) {

					for (int i = 0;
						 i <
						((Var) outputs.get(0)).field.vars.size();
						 i++) {
						s1 = ((ClassField) op.field.vars.get(i)).toString();
						assigns += "        " + getObject(op.object) + s1 +
							" = " +
							ip + "[" + Integer.toString(i) + "];\n";
					}
					return assigns;
				}
				if (op.field.isArray() && ip.field.isAlias()) {

					assigns += op.field.type + " TEMP" +
						Integer.toString(aliasNr) +
						" = new " +
						op.field.arrayType() + "[" + ip.field.vars.size() +
						"];\n";
					for (int i = 0; i < ip.field.vars.size(); i++) {
						s1 = ((ClassField) ip.field.vars.get(i)).toString();
						assigns += "        " + " TEMP" +
							Integer.toString(aliasNr) + "[" +
							Integer.toString(i) + "] = " +
							getObject(ip.object) + s1 +
							";\n";
					}
					assigns += "        " + op + " = " + " TEMP" +
						Integer.toString(aliasNr);
					//RelType.auxVarCounter++;
					return assigns;
				}
			}

			Var var;
			String m = new String(method + " ");
			String left = "";
			String left2 = "";
			String right = "";
			ArrayList ajut = new ArrayList();
			for (int i = 0; i < inputs.size(); i++) {
				var = (Var) inputs.get(i);
				pattern = Pattern.compile("([^a-zA-Z_])(([a-zA-Z_0-9]+\\.)*)" +
					var.name + "([^a-zA-Z0-9_])");
				matcher = pattern.matcher(m);
				if (matcher.find()) {
					left = matcher.group(1);
					left2 = matcher.group(2);
					right = matcher.group(4);
				}
				ajut.add(new AjutHack(var.name, "#" + Integer.toString(i)));
				m = m.replaceFirst("([^a-zA-Z_]" + left2 + var.name +
					"[^a-zA-Z0-9_])",
					left + getObject(var.object) + "#" + Integer.toString(i) + right);
			}

			for (int i = 0; i < inputs.size(); i++) {
				AjutHack paar = (AjutHack) ajut.get(i);
				m = m.replaceFirst(paar.repl, paar.var);
			}

			left2 = "";
			var = (Var) outputs.get(0);
			pattern = Pattern.compile("([^a-zA-Z_]?)(([a-zA-Z_0-9]+\\.)*)" +
				var.name + "([^a-zA-Z0-9_])");
			matcher = pattern.matcher(m);
			if (matcher.find()) {
				left = matcher.group(1);
				left2 = matcher.group(2);
				right = matcher.group(4);
			}

			m = m.replaceFirst("([^a-zA-Z_]?" + left2 + var.name +
				"[^a-zA-Z0-9_])",
				left + getObject(var.object) + var.name +
				right);

			if (((Var) outputs.get(0)).type.equals("int") &&
				(!getMaxType(inputs).equals("int") ||
				method.indexOf(".") >= 0)) {
				m = m.replaceFirst("=", "= (int)(") + ")";

			}
			return m;
		} else if (type == 5) {
			return (Var) outputs.get(0) + " = " + (Var) inputs.get(0);

		} else if (type == 6) {
			if (!outputs.isEmpty()) {
				return ((Var) outputs.get(0) + " = " + getObject(object) +
					method + getSubtaskParameters()); // + getParameters()
			} else {
				return (getObject(object) + method + getSubtaskParameters()); // + getParameters()
			}
		} else {
			db.p(method);
			String s1, s2, assigns = "";
			Var ip = (Var) inputs.get(0);
			Var op = (Var) outputs.get(0);

			if (ip.field.isArray() && op.field.isAlias()) {

				for (int i = 0;
					 i < ((Var) outputs.get(0)).field.vars.size();
					 i++) {
					s1 = (String) ((Var) outputs.get(0)).field.vars.get(
						i);
					assigns += "        " + getObject(op.object) + s1 + " = " +
						ip +
						"[" + Integer.toString(i) + "];\n";
				}
				return assigns;
			}
			if (op.field.isArray() && ip.field.isAlias()) {
				for (int i = 0; i < ip.field.vars.size(); i++) {
					s1 = (String) ip.field.vars.get(i);
					assigns += "        " + op + "[" + Integer.toString(i) +
						"] = " + getObject(ip.object) + s1 + ";\n";
				}
				return assigns;
			}
			if (op.field.isAlias() && ip.field.isAlias()) {
				for (int i = 0; i < ip.field.vars.size(); i++) {
					s1 = (String) ip.field.vars.get(i);
					s2 = (String) op.field.vars.get(i);

					assigns += "        " + getObject(op.object) + s2 + " = " +
						getObject(ip.object) + s1 + ";\n";
				}
				return assigns;
			}

			return op + " = " + ip;
		}
	}

	public boolean equals(Object e) {
		return this.toString().equals(((Rel) e).toString());
	}

	public Object clone() {
		try {
			Rel rel = (Rel) super.clone();

			return rel;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}


//	public Object clone()
//	{
//	    try {
//            Rel rel = (Rel) super.clone();
//
//            rel.outputs = (ArrayList)outputs.clone();
//            for (int i = 0; i < rel.outputs.size(); i++) {
//                Var var = (Var)rel.outputs.get(i);
//                var = (Var)var.clone();
//                rel.outputs.set(i, var);
//            }
//
//            rel.inputs = (ArrayList)inputs.clone();
//            for (int i = 0; i < rel.inputs.size(); i++) {
//                Var var = (Var)rel.inputs.get(i);
//                var = (Var)var.clone();
//                rel.inputs.set(i, var);
//            }
//
//            rel.subtasks = (ArrayList)subtasks.clone();
//            for (int i = 0; i < rel.subtasks.size(); i++) {
//                Var var = (Var)rel.subtasks.get(i);
//                var = (Var)var.clone();
//                rel.subtasks.set(i, var);
//            }
//
//            rel.unknownInputs = rel.inputs.size();
//    		rel.subtaskFlag = rel.subtasks.size();
//
//    		rel.inAlgorithm = false;
//
//    		return rel;
//
//        } catch (CloneNotSupportedException e) {
//            return null;
//        }
//	}
}


class AjutHack {
	String var;
	String repl;

	public AjutHack(String name, String s) {
		var = name;
		repl = s;
	}
}
