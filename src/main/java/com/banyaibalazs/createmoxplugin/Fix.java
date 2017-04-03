package com.banyaibalazs.createmoxplugin;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Fix implements ThrowableRunnable<RuntimeException> {
    private final Project project;
    private final PsiFile file;
    private final PsiClass clazz;
    private final PsiParameterList psiParameterList;
    private final PsiConstructorCall call;

    Fix(Project project, PsiFile file, PsiClass targetClazz, PsiConstructorCall call) {
        this.project = project;
        this.file = file;
        this.clazz = targetClazz;
        this.psiParameterList = call.resolveConstructor().getParameterList();
        this.call = call;
    }

    @Override
    public void run() {
        Stream.of(psiParameterList.getParameters())
                .map(Mock::fromParameter)
                .map(mock -> mock.asField(project))
                .filter(Objects::nonNull)
                .filter(this::classHasField)
                .peek(clazz::add)
                .map(NavigationItem::getName)
                .collect(Collectors.toSet())
                .forEach(this::createConstructorCall);
    }

    private void createConstructorCall(String argumentList) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(file);

        if (document != null) {
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);

            PsiExpressionList args = call.getArgumentList();
            if (args != null) {
                int beginningOfStatement = args.getTextOffset();

                document.deleteString(beginningOfStatement, beginningOfStatement + args.getTextLength());
                document.insertString(beginningOfStatement, "(" + argumentList + ");");
            }
        }
    }

    private boolean classHasField(@NotNull PsiField field) {
        return !Stream.of(clazz.getFields())
                .anyMatch(psiField -> psiField.getName().equals(field.getName()));
    }


}
