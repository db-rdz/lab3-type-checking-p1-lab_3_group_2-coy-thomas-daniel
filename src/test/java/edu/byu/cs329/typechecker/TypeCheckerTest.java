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

  private List<DynamicNode> generateProof(final String fileName, ISymbolTable symbolTable) {
    URI fileURI = getURI(fileName);
    ASTNode cu = Parser.parse(fileURI);

    // TODO: instantiate visitor, call cu.accept(visitor), and return proof
    TypeCheckerVisitor visitor = new TypeCheckerVisitor();
    //cu.accept(visitor);
    return null;
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
      List<DynamicNode> proof = generateProof(fileName, symbolTable);
      long containerCount = getContainerCount(proof.stream());
      Assertions.assertEquals(1, containerCount);
    }

    @Test
    @DisplayName("Should create one test when given an empty class")
    void Should_createOneTest_When_givenEmptyClass() {
      List<DynamicNode> proof = generateProof(fileName, symbolTable);
      long testCount = getTestCount(proof.stream());
      Assertions.assertEquals(1, testCount);
    }

    @TestFactory
    @DisplayName("Should prove type safe when given an empty class")
    Stream<DynamicNode> Should_proveTypeSafe_When_givenEmptyClass() {
      return generateProof("EmptyClass.java", symbolTable).stream();
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
      List<DynamicNode> proof = generateProof(fileName, symbolTable);
      long containerCount = getContainerCount(proof.stream());
      Assertions.assertEquals(3, containerCount);
    }

    @Test
    @DisplayName("Should create three containers when given a class with one method with empty statement")
    void Should_createOneTest_When_givenOneMethodWithEmptyStatement() {
      List<DynamicNode> proof = generateProof(fileName, symbolTable);
      long testCount = getTestCount(proof.stream());
      Assertions.assertEquals(1, testCount);
    }

    @TestFactory
    Stream<DynamicNode> Should_proveTypeSafe_When_givenOneMethodWithEmptyStatement() {
      return generateProof(fileName, symbolTable).stream();
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
      List<DynamicNode> proof = generateProof(fileName, symbolTable);
      long containerCount = getContainerCount(proof.stream());
      Assertions.assertEquals(7, containerCount);
    }

    @Test
    @DisplayName("Should create three containers when given a class with three methods with empty statements")
    void Should_createOneTest_When_givenMultipleMethodsWithEmptyStatement() {
      List<DynamicNode> proof = generateProof(fileName, symbolTable);
      long testCount = getTestCount(proof.stream());
      Assertions.assertEquals(1, testCount);
    }

    @TestFactory
    Stream<DynamicNode> Should_proveTypeSafe_When_givenMultipleMethodsWithEmptyStatement() {
      return generateProof(fileName, symbolTable).stream();
    }
  }
}
