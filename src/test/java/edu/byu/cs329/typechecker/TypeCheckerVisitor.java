package edu.byu.cs329.typechecker;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;

public class TypeCheckerVisitor extends ASTVisitor {
  private Deque<List<DynamicNode>> proofStack = null;
  private List<DynamicNode> proofs = new ArrayList<>();
  private ISymbolTable symbolTable = null;
  private Stack<String> className = new Stack<>();
  private String methodName = null;
  private List<List<String>> frames = new ArrayList<>();

  public TypeCheckerVisitor(ISymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  public List<DynamicNode> popProof() {
    return proofStack.pop();
  }

  private void pushProof(List<DynamicNode> proof) {
    proofStack.push(proof);
  }

  private List<DynamicNode> peekProof() {
    return proofStack.peek();
  }
  
  private void pushFrame(ArrayList<String> list) {
    this.frames.add(list);
  }
  
  private List<String> popFrame() {
    if (!this.frames.isEmpty()) {
      return this.frames.remove(this.frames.size() - 1);
    }
    return null;
  }
  
  private List<String> peekFrame(){
    if (!this.frames.isEmpty()) {
      return this.frames.get(this.frames.size() - 1);
    }
    return null;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    className.push(node.getName().getIdentifier());
    pushProof(new ArrayList<>());
    return super.visit(node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    className.pop();
    String displayName = "E |- " + node;
    createProofAndAddToObligations(displayName);
    super.endVisit(node);
  }

  private void createProofAndAddToObligations(String displayName) {
    List<DynamicNode> proofs = popProof();
    addNoObligationIfEmpty(proofs);
    DynamicContainer proof = DynamicContainer.dynamicContainer(displayName, proofs.stream());
    List<DynamicNode> obligations = peekProof();
    obligations.add(proof);
  }

  private void addNoObligationIfEmpty(List<DynamicNode> proofs) {
    if (proofs.size() > 0) {
      return;
    }

    proofs.add(generateNoObligation());
  }

  private DynamicNode generateNoObligation() {
    return DynamicTest.dynamicTest("E |- true", () -> Assertions.assertTrue(true));
  }

  private void setMethodName(final String name) {
    methodName = name;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    pushProof(new ArrayList<>());
    setMethodName(node.getName().getIdentifier());
    return super.visit(node);
  }

  @Override
  public boolean visit(EmptyStatement node) {
    pushProof(new ArrayList<>());
    return super.visit(node);
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    String displayName = "E |- " + node;
    createProofAndAddToObligations(displayName);
    super.endVisit(node);
  }

  @Override
  public void endVisit(EmptyStatement node) {
    String displayName = "E |- " + node;
    createProofAndAddToObligations(displayName);
    super.endVisit(node);
  }

  @Override
  public boolean visit(Block node) {
    pushFrame(new ArrayList<String>());
    return super.visit(node);
  }


  @Override
  public void endVisit(Block node) {
    List<String> frame = popFrame();
    symbolTable.removeLocals(frame);
  }

  
  @Override
  public boolean visit(VariableDeclarationFragment node) {
    pushProof(new ArrayList<>());

    String name = node.getName().getIdentifier();
    if (node.getParent() instanceof VariableDeclarationStatement) {
      VariableDeclarationStatement parent = (VariableDeclarationStatement)(node.getParent());
      String type = parent.getType().toString();
      symbolTable.addLocal(name, type);
      List<String> frame = peekFrame();
      frame.add(name);
    }

    Expression e = node.getInitializer();
    if (e != null) {
      return assignmentObligations(name, e);
    }

    return false;
  }

  private boolean assignmentObligations(String name, Expression e) {
    String localType = symbolTable.getLocalType(name);
    if (localType != null) {
      if(e.getClass().isInstance(NumberLiteral.class) && isNumberType(localType)) {
        return true;
      }
      if(e.getClass().isInstance(BooleanLiteral.class) && (localType == "boolean" || localType == "Boolean")) {
        return true;
      }
      if(e.getClass().isInstance(StringLiteral.class) && localType == "String") {
        return true;
      }
    }
    return false;
  }

  private boolean isNumberType(String localType) {
    return (localType == "int") || (localType == "short") || (localType == "Int") || (localType == "Short");
  }

  public List<DynamicNode> getProofs() {
    return proofs;
  }
}
