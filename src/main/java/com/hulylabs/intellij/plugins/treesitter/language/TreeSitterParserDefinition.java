package com.hulylabs.intellij.plugins.treesitter.language;

import com.hulylabs.intellij.plugins.treesitter.language.psi.TreeSitterFile;
import com.hulylabs.intellij.plugins.treesitter.language.psi.TreeSitterParser;
import com.hulylabs.intellij.plugins.treesitter.language.psi.TreeSitterPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.EmptyLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class TreeSitterParserDefinition implements ParserDefinition {
    private final static IFileElementType FILE_ELEMENT_TYPE = new IFileElementType("TreeSitter", TreeSitterLanguage.INSTANCE);
    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new EmptyLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new TreeSitterParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE_ELEMENT_TYPE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return new TreeSitterPsiElement(node.getElementType());
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new TreeSitterFile(viewProvider);
    }
}
