package com.banyaibalazs.createmoxplugin;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Fix implements ThrowableRunnable<RuntimeException> {
    private final Project project;
    private final PsiFile file;
    private final PsiClass clazz;
    private final PsiParameterList psiParameterList;
    private final PsiConstructorCall call;

    public Fix(Project project, PsiFile file, PsiClass clazz, PsiParameterList psiParameterList, PsiConstructorCall call) {
        this.project = project;
        this.file = file;
        this.clazz = clazz;
        this.psiParameterList = psiParameterList;
        this.call = call;
    }

    @Override
    public void run() {
        List<Mock> mocks = Stream.of(psiParameterList.getParameters())
                .map(Mock::fromParameter)
                .collect(Collectors.toList());
        createMockFields(project, mocks);
        createConstructorCall(project, file, mocks);
    }

    private void createMockFields(Project project, List<Mock> mocks) {
        mocks.stream()
                .map(mock -> mock.asField(project))
                .filter(field -> field != null)
                .filter(field -> !hasField(clazz, field))
                .forEach(clazz::add);
    }

    private void createConstructorCall(Project project, PsiFile file, List<Mock> mocks) {

        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(file);


        if (document != null) {
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);

            PsiExpressionList args = call.getArgumentList();
            int beginningOfStatement = args.getTextOffset();

            document.deleteString(beginningOfStatement, beginningOfStatement + args.getTextLength());

            String argumentList = mocks.stream()
                    .map(Mock::name)
                    .collect(Collectors.joining(", "));

            document.insertString(beginningOfStatement, "(" + argumentList + ");");
        }

    }

    private boolean hasField(PsiClass clazz, @NotNull PsiField field) {
        return Stream.of(clazz.getFields()).anyMatch(psiField -> psiField.getName().equals(field.getName()));
    }


}
