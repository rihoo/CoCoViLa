package ee.ioc.cs.vsle.editor;

import ee.ioc.cs.vsle.vclass.ObjectList;
import ee.ioc.cs.vsle.vclass.GObj;
import ee.ioc.cs.vsle.vclass.ClassField;
import ee.ioc.cs.vsle.ccl.CompileException;
import ee.ioc.cs.vsle.ccl.CCL;
import ee.ioc.cs.vsle.util.db;
import ee.ioc.cs.vsle.synthesize.Var;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import javax.swing.JTextArea;

/**
 */
public class ProgramRunner {
	Object genObject;
	public static HashSet foundVars = new HashSet();

	void runPropagate(Object genObject, ObjectList objects) {
		try {
			Class clasType;
			Class clas = genObject.getClass();

			/* Method method = clas.getMethod("compute", null);
			 method.invoke(genObject, null);*/
			Field f, f2;
			Object lastObj;
			GObj obj;
			ClassField field;
			int in;
			double d;
			float fl;
			boolean b;

			String fullName;
			Var var;
			boolean varIsComputed;
			// ee.ioc.cs.editor.util.db.p(genClass.getClass().getFields()[0]);
			for (int i = 0; i < objects.size(); i++) {
				obj = (GObj) objects.get(i);
				f = clas.getDeclaredField(obj.name);
				lastObj = f.get(genObject);
				for (int j = 0; j < obj.fields.size(); j++) {
					field = (ClassField) obj.fields.get(j);
					if (!field.type.equals("alias")) {
						clasType = f.getType();
						f2 = clasType.getDeclaredField(field.name);
						Class c = f2.getType();
						fullName = obj.name + "." + field.name;
						varIsComputed = false;
						if (foundVars != null) {
							Iterator allVarsIter = foundVars.iterator();
							while (allVarsIter.hasNext()) {
								var = (Var) allVarsIter.next();

								if (fullName.equals(var.object.toString().substring(5) + "." + var.field)) {
									varIsComputed = true;
								}
							}
						}
						if (varIsComputed) {
							if (c.toString().equals("int")) {
								in = f2.getInt(lastObj);
								field.value = Integer.toString(in);
							} else if (c.toString().equals("double")) {
								d = f2.getDouble(lastObj);
								field.value = Double.toString(d);
							} else if (c.toString().equals("boolean")) {
								b = f2.getBoolean(lastObj);
								field.value = Boolean.toString(b);
							} else if (c.toString().equals("char")) {
								char ch = f2.getChar(lastObj);
								field.value = Character.toString(ch);
							} else if (c.toString().equals("float")) {
								fl = f2.getFloat(lastObj);
								field.value = Float.toString(fl);
							} else {// it is type object
								Object o = f2.get(lastObj);
								field.value = o.toString();
							}
						}
						//field.updateGraphics();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	Object compileAndRun(String programName, ArrayList watchFields,
						 JTextArea runResultArea) throws CompileException {
		genObject = makeGeneratedObject(programName);
		run(watchFields, runResultArea);
		return genObject;
	}

	void run(ArrayList watchFields, JTextArea runResultArea) {
		try {
			Class clas = genObject.getClass();
			Method method = clas.getMethod("compute", null);

			method.invoke(genObject, null);
			Field f;
			StringTokenizer st;
			Object lastObj;

			// ee.ioc.cs.editor.util.db.p(genClass.getClass().getFields()[0]);
			for (int i = 0; i < watchFields.size(); i++) {
				lastObj = genObject;
				clas = genObject.getClass();
				st = new StringTokenizer((String) watchFields.get(i), ".");
				while (st.hasMoreElements()) {
					String s = st.nextToken();

					db.p(s);
					f = clas.getDeclaredField(s);
					if (st.hasMoreElements()) {
						clas = f.getType();
						lastObj = f.get(lastObj);
					} else {
						Class c = f.getType();

						if (c.toString().equals("int")) {
							// textArea.append((String)watchFields.get(i) +": "+f.getInt(lastObj)+"\n");
							runResultArea.append(
									(String) watchFields.get(i) + ": "
									+ f.getInt(lastObj) + "\n");
						} else if (c.toString().equals("double")) {
							runResultArea.append(
									(String) watchFields.get(i) + ": "
									+ f.getDouble(lastObj) + "\n");
						} else if (c.toString().equals("boolean")) {
							runResultArea.append(
									(String) watchFields.get(i) + ": "
									+ f.getBoolean(lastObj) + "\n");
						} else if (c.toString().equals("char")) {
							runResultArea.append(
									(String) watchFields.get(i) + ": "
									+ f.getChar(lastObj) + "\n");
						} else if (c.toString().equals("float")) {
							runResultArea.append(
									(String) watchFields.get(i) + ": "
									+ f.getFloat(lastObj) + "\n");
						} else {
							runResultArea.append(
									(String) watchFields.get(i) + ": "
									+ f.get(lastObj) + "\n");
						}
					}

				}
			}
			runResultArea.append("----------------------\n");

		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	Object makeGeneratedObject(String programName) throws CompileException {
		CCL classLoader = new CCL();

		try {
			classLoader.compile(programName);
			Class clas = classLoader.loadClass(programName);
			Object o = clas.newInstance();

			return o;
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}

}
