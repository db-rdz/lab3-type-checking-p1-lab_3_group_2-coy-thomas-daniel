package edu.byu.cs329.typechecker;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * The Suite of tests for the Type Checker
 *
 */
public class TypeCheckerTest {
  static final Logger log = LoggerFactory.getLogger(TypeCheckerTest.class);

  private static URI getURI(final String fileName) {
    URL url = ClassLoader.getSystemResource(fileName);
    Objects.requireNonNull(url, "\'" + fileName + "\'" + " not found in classpath");
    URI uri = null;
    try {
      uri = url.toURI();
    } catch (URISyntaxException e) {
      log.error("Failed to get URI for" + fileName);
      e.printStackTrace();
    }
    return uri;
  }

  private List<DynamicNode> generateProofs(final String fileName, ISymbolTable symbolTable) {
    URI fileURI = getURI(fileName);
    ASTNode cu = Parser.parse(fileURI);

    TypeCheckerVisitor visitor = new TypeCheckerVisitor(symbolTable);
    cu.accept(visitor);
    return visitor.getProofs();
  }
  
  private long getContainerCount(final Stream<? extends DynamicNode> proof) {
    return proof.filter(t -> t instanceof DynamicContainer)
        .mapToLong(v -> 1 + getContainerCount(((DynamicContainer) v).getChildren())).sum();
  }

  private long getTestCount(final Stream<? extends DynamicNode> proof) {
    return proof.mapToLong(t -> {
      if (t instanceof DynamicTest) {
        return 1;
      }
      return getTestCount(((DynamicContainer) t).getChildren());
    }).sum();
  }
  
  @Nested
  @DisplayName("Tests Empty Class")
  class EmptyClassTests {

    String fileName = "EmptyClass.java";
    ISymbolTable symbolTable = Mockito.mock(ISymbolTable.class);

    @Test
    @DisplayName("Should create one container when given an empty class")
    void Should_createOneContainer_When_givenEmptyClass() {
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      long containerCount = getContainerCount(proof.stream());
      Assertions.assertEquals(1, containerCount);
    }

    @Test
    @DisplayName("Should create one test when given an empty class")
    void Should_createOneTest_When_givenEmptyClass() {
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      long testCount = getTestCount(proof.stream());
      Assertions.assertEquals(1, testCount);
    }

    @TestFactory
    @DisplayName("Should prove type safe when given an empty class")
    Stream<DynamicNode> Should_proveTypeSafe_When_givenEmptyClass() {
      return generateProofs("EmptyClass.java", symbolTable).stream();
    }
  }
  
  
  
  @Nested
  @DisplayName("Tests class with one method with empty statement")
  class OneEmptyMethodClassTests {

    String fileName = "OneMethodWithEmptyStatement.java";
    ISymbolTable symbolTable = Mockito.mock(ISymbolTable.class);

    @Test
    @DisplayName("Should create three containers when given a class with one method with empty statement")
    void Should_createThreeContainers_When_givenOneMethodWithEmptyStatement() {   
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      long containerCount = getContainerCount(proof.stream());
      Assertions.assertEquals(3, containerCount);
    }

    @Test
    @DisplayName("Should create three containers when given a class with one method with empty statement")
    void Should_createOneTest_When_givenOneMethodWithEmptyStatement() {
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      long testCount = getTestCount(proof.stream());
      Assertions.assertEquals(3, testCount);
    }

    @TestFactory
    Stream<DynamicNode> Should_proveTypeSafe_When_givenOneMethodWithEmptyStatement() {
      return generateProofs(fileName, symbolTable).stream();
    }
  }
  
  
  
  @Nested
  @DisplayName("Test class with multiple methods that are empty")
  class MultipleEmptyMethodTestClass {
    String fileName = "MultipleMethodsWithEmptyStatement.java";
    ISymbolTable symbolTable = Mockito.mock(ISymbolTable.class);
    
    @Test
    @DisplayName("Should create seven containers when given a class with three methods with empty statements")
    void Should_createThreeContainers_When_givenMultipleMethodsWithEmptyStatement() {   
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      long containerCount = getContainerCount(proof.stream());
      Assertions.assertEquals(7, containerCount);
    }

    @Test
    @DisplayName("Should create three containers when given a class with three methods with empty statements")
    void Should_createOneTest_When_givenMultipleMethodsWithEmptyStatement() {
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      long testCount = getTestCount(proof.stream());
      Assertions.assertEquals(1, testCount);
    }

    @TestFactory
    Stream<DynamicNode> Should_proveTypeSafe_When_givenMultipleMethodsWithEmptyStatement() {
      return generateProofs(fileName, symbolTable).stream();
    }
  }
  
  @Nested
  @DisplayName("Test for CompliationUnits")
  class CompilationUnitsTests {
    String fileName = "EmptyClass.java";
    ISymbolTable symbolTable = Mockito.mock(ISymbolTable.class);
    
    @Test
    @DisplayName("")
    void Should_createCorrectCompilationUnit_When_givenAnyClassFile() {   
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      //TODO: Check Compilation Unit here
    }

    @TestFactory
    Stream<DynamicNode> Should_proveTypeSafe_When_givenSomething() {
      return generateProofs(fileName, symbolTable).stream();
    }
  }
  
  @Nested
  @DisplayName("Tests for Type Declarations")
  class TypeDeclarationTests {
    @Test
    @DisplayName("Test inappropriate value assigned to Boolean type")
    void Should_notReturnTypeCorrectProof_When_givenFileAssigningWrongObjectToBooleanType() {
      String fileName = "WrongTypeToBoolean.java";
      ISymbolTable symbolTable = Mockito.mock(ISymbolTable.class);
      
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      //TODO: Verify that the proof does not say type correct
    }
    
    @Test
    @DisplayName("Test appropriate value assigned to Boolean type")
    void Should_doSomething_When_givenSomething() {
      String fileName = "RightTypeToBoolean.java";
      ISymbolTable symbolTable = Mockito.mock(ISymbolTable.class);
      
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      //TODO: verify that the proof is type safe
    }
    
    //TODO: More tests here for the Literal types short, int, and String
  }
  
  @Nested
  @DisplayName("Tests for Method Statements")
  class MethodTests {
    String fileName = "";
    ISymbolTable symbolTable = Mockito.mock(ISymbolTable.class);
    
    @Test
    @DisplayName("")
    void Should_doSomething_When_givenSomething() {   
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      long containerCount = getContainerCount(proof.stream());
      Assertions.assertEquals(7, containerCount);
    }

    @TestFactory
    Stream<DynamicNode> Should_proveTypeSafe_When_givenSomething() {
      return generateProofs(fileName, symbolTable).stream();
    }
  }
  
  @Nested
  @DisplayName("Tests for Block Statements")
  class BlockStatementTests {
    String fileName = "";
    ISymbolTable symbolTable = Mockito.mock(ISymbolTable.class);
    
    @Test
    @DisplayName("")
    void Should_doSomething_When_givenSomething() {   
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      long containerCount = getContainerCount(proof.stream());
      Assertions.assertEquals(7, containerCount);
    }

    @TestFactory
    Stream<DynamicNode> Should_proveTypeSafe_When_givenSomething() {
      return generateProofs(fileName, symbolTable).stream();
    }
  }
  
  @Nested
  @DisplayName("Tests for Variable Declaration Fragments")
  class VariableDeclarationFragmentTests{
    String fileName = "";
    ISymbolTable symbolTable = Mockito.mock(ISymbolTable.class);
    
    @Test
    @DisplayName("")
    void Should_doSomething_When_givenSomething() {   
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      long containerCount = getContainerCount(proof.stream());
      Assertions.assertEquals(7, containerCount);
    }

    @TestFactory
    Stream<DynamicNode> Should_proveTypeSafe_When_givenSomething() {
      return generateProofs(fileName, symbolTable).stream();
    }
  }
  
  @Nested
  @DisplayName("Tests for Types of Literals")
  class LiteralTypeTests {
    String fileName = "";
    ISymbolTable symbolTable = Mockito.mock(ISymbolTable.class);
    
    @Test
    @DisplayName("")
    void Should_doSomething_When_givenSomething() {   
      List<DynamicNode> proof = generateProofs(fileName, symbolTable);
      long containerCount = getContainerCount(proof.stream());
      Assertions.assertEquals(7, containerCount);
    }

    @TestFactory
    Stream<DynamicNode> Should_proveTypeSafe_When_givenSomething() {
      return generateProofs(fileName, symbolTable).stream();
    }
  }
}
