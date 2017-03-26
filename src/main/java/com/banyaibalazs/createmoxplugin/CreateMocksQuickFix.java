package com.banyaibalazs.createmoxplugin;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CreateMocksQuickFix extends BaseIntentionAction {
    private PsiClass clazz;
    private PsiParameterList psiParameterList;
    private PsiConstructorCall call;

    CreateMocksQuickFix(PsiClass clazz, PsiParameterList psiParameterList, PsiConstructorCall call) {
        this.clazz = clazz;
        this.psiParameterList = psiParameterList;
        this.call = call;
    }

    @NotNull
    @Override
    public String getText() {
        return "Create mocks";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Mocking";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, PsiFile file) throws
            IncorrectOperationException {

        ApplicationManager.getApplication().invokeLater(() -> {
            ApplicationManager.getApplication().assertReadAccessAllowed();

            PsiDocumentManager.getInstance(project).commitAllDocuments();

            if(!clazz.isValid()){
                return;
            }

            CommandProcessor.getInstance().executeCommand(
                    project,
                    () -> ApplicationManager.getApplication().runWriteAction(() -> applyFix(project, file)
                    ),
                    "Create mocks",
                    null
            );

        });
    }

    private void applyFix(Project project, PsiFile file) {
        List<Mock> mocks = Stream.of(psiParameterList.getParameters())
                .map(Mock::fromParameter)
                .collect(Collectors.toList());
        createMockFields(project, mocks);
        createConstructorCall(project, file, mocks);
    }

    private void createConstructorCall(Project project, PsiFile file, List<Mock> mocks) {
        PsiExpressionList args = call.getArgumentList();
        int beginningOfStatement = args.getTextOffset();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(file);
        document.deleteString(beginningOfStatement, args.getTextOffset()+args.getTextLength());

        String argumentList = mocks.stream()
                .map(Mock::name)
                .collect(Collectors.joining(", "));

        document.insertString(beginningOfStatement, "("+argumentList+")");

    }

    private void createMockFields(Project project, List<Mock> mocks) {
        mocks.stream()
                .map(mock -> mock.asField(project))
                .filter(field -> field != null)
                .filter(field -> !hasField(clazz, field))
                .forEach(field -> clazz.add(field));
    }

    private boolean hasField(PsiClass clazz, @NotNull PsiField field) {
        return Stream.of(clazz.getFields()).anyMatch(psiField -> psiField.getName().equals(field.getName()));
    }

}