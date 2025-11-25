package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class LoopWhileCommand extends Command {

    public LoopWhileCommand(Command c1, Expression e, Command c2, SourcePosition position) {
        super(position);
        C1 = c1;
        E = e;
        C2 = c2;
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> v, TArg arg) {
        return v.visitLoopWhileCommand(this, arg);
    }

    public final Command C1;
    public Expression E;
    public final Command C2;
}