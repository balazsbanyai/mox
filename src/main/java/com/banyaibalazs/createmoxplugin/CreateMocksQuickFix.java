package com.banyaibalazs.createmoxplugin;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

class CreateMocksQuickFix extends BaseIntentionAction {
    private PsiClass clazz;
    private PsiConstructorCall call;

    CreateMocksQuickFix(PsiClass clazz, PsiConstructorCall call) {
        this.clazz = clazz;
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

            Fix fix = new Fix(project, file, clazz, call);



            CommandProcessor.getInstance().executeCommand(
                    project,
                    () -> WriteAction.run(fix),
                    "Create mocks",
                    null
            );


        });
    }



}