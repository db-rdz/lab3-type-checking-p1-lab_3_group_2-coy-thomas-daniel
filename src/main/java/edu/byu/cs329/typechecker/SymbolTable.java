package edu.byu.cs329.typechecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;
import org.javatuples.Triplet;

public class SymbolTable implements ISymbolTable {
  //Contains the pairs of local variables and their types, 
  //p1 is the token name and p2 is the type tied to it.
  private List<Pair<String, String>> locals = new ArrayList<>();

  //This is a map of classModels key: classname, value: model object, 
  //containing lists of the following:
  //Fields
  //Methods
  //Parameters
  Map<String, ClassObject> classes = new HashMap<>();


  public SymbolTable() {}

  @Override
  public String getNumberLiteralType(String token) {
    try {
      Short.parseShort(token);
      return SHORT;
    } catch (NumberFormatException e) {
      try {
        Integer.parseInt(token);
        return INT;
      } catch (NumberFormatException ex) {
        return null;
      }
    }
  }

  @Override
  public String getFieldType(String className, String fieldName) {
    for (Pair<String, String> field : classes.get(className).getFields()) {
      if (field.getValue0() == fieldName) {
        return field.getValue1();
      }
    }
    return null;
  }

  @Override
  public String getMethodReturnType(String className, String methodName) {
    for (Triplet<String, String, List<Pair<String, String>>> method
        : classes.get(className).getMethods()) {
      if (method.getValue0() == methodName) {
        return method.getValue1();
      }
    }
    return null;
  }

  @Override
  public String getParameterType(String className, String methodName, String paramName) {
    for (Triplet<String, String, List<Pair<String, String>>> method
        : classes.get(className).getMethods()) {
      if (method.getValue0() == methodName) {
        for (Pair<String, String> param : method.getValue2()) {
          if (param.getValue0() == paramName) {
            return param.getValue1();
          }
        }
      }
    }
    return null;
  }

  @Override
  public String getLocalType(String name) {
    for (int i = this.locals.size() - 1; i >= 0; i--) {
      Pair<String, String> p = this.locals.get(i);
      if (p.getValue0() == name) {
        return p.getValue1();
      }
    }
    return null;
  }

  @Override
  public boolean classExists(String className) {
    return classes.containsKey(className);
  }

  @Override
  public boolean fieldExists(String className, String fieldName) {
    for (Pair<String, String> field : classes.get(className).getFields()) {
      if (field.getValue0() == fieldName) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean methodExists(String className, String methodName) {
    for (Triplet<String, String, List<Pair<String, String>>> method
        : classes.get(className).getMethods()) {
      if (method.getValue0() == methodName) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean parameterExists(String className, String methodName, String paramName) {
    for (Triplet<String, String, List<Pair<String, String>>> method
        : classes.get(className).getMethods()) {
      if (method.getValue0() == methodName) {
        for (Pair<String, String> param : method.getValue2()) {
          if (param.getValue0() == paramName) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean localExists(String name) {
    for (int i = this.locals.size() - 1; i >= 0; i--) {
      Pair<String, String> p = this.locals.get(i);
      if (p.getValue0() == name) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean addLocal(String name, String type) {
    if (!isValidType(type)) {
      throw new IllegalArgumentException();
    }
    return this.locals.add(new Pair<String, String>(name, type));
  }

  private boolean isValidType(String type) {
    if (type == NO_TYPE) {
      return false;
    }
    return type == SHORT || type == SHORT_OBJECT 
        || type == INT || type == INT_OBJECT
        || type == BOOL || type == BOOL_OBJECT
        || type == STRING_OBJECT || type == OBJECT;
  }

  @Override
  public void removeLocals(List<String> locals) {
    for (String l : locals) {
      for (int i = this.locals.size() - 1; i >= 0; i--) {
        Pair<String, String> p = this.locals.get(i);
        if (p.getValue0() == l) {
          locals.remove(i);
        }
      }
    }
  }

  public void addClass(String className) {
    this.classes.put(className, new ClassObject(className));
  }

  private class ClassObject {
    private String name = null;
    //Fields: 0: Name, 1:Type
    private List<Pair<String, String>> fields = new ArrayList<>();

    //Methods: 0: Method Name, 1: Method Return Type, 2: Method Paramaters, 
    //which are Pairs of Names and Types
    private List<Triplet<String, String, List<Pair<String, String>>>> methods = new ArrayList<>();

    //This list contains the names of classes in the Map that are nested in this class.
    private List<String> nestedClasses = new ArrayList<>();

    public ClassObject(String name) {
      this.name = name;
    }

    public List<Pair<String, String>> getFields() {
      return fields;
    }

    public void addField(String name, String type) {
      this.fields.add(new Pair<String, String>(name, type));
    }

    public String getName() {
      return name;
    }

    public List<Triplet<String, String, List<Pair<String, String>>>> getMethods() {
      return methods;
    }

    public void addMethod(String methodName, 
        String methodReturnType, List<Pair<String, String>> paramaters) {
      this.methods.add(new Triplet<String, String, 
          List<Pair<String, String>>>(methodName, methodReturnType,paramaters));
    }

    public void addParameter(String methodName, 
        String parameterName, String parameterType) {
      for (Triplet<String, String, List<Pair<String, String>>> method : this.methods) {
        if (method.getValue0() == methodName) {
          method.getValue2().add(new Pair<String, String>(parameterName, parameterType));
        }
      }
    }

    public void addNestedClass(String nestedClassName) {
      this.nestedClasses.add(nestedClassName);
    }

    public List<String> getNestedClass() {
      return this.nestedClasses;
    }
  }

}
