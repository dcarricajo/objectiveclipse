package org.eclipse.cdt.objc.core.internal.parser.scanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree.IASTInclusionNode;

/**
 * Various location contexts which are suitable for interpreting local offsets.
 * These offsets are converted in a global sequence-number to make all ast nodes
 * comparable with each other.
 * 
 * @since 5.0
 */
abstract class LocationCtx implements ILocationCtx {
    /**
     * The end-offset of the denotation of this context in the parent's source.
     * This is no sequence number.
     */
    final int fEndOffsetInParent;
    /**
     * The offset of the denotation of this context in the parent's source. This
     * is no sequence number.
     */
    final int fOffsetInParent;
    final LocationCtxContainer fParent;
    /**
     * The first sequence number used by this context.
     */
    final int fSequenceNumber;

    public LocationCtx(LocationCtxContainer parent, int parentOffset, int parentEndOffset, int sequenceNumber) {
        fParent = parent;
        fOffsetInParent = parentOffset;
        fEndOffsetInParent = parentEndOffset;
        fSequenceNumber = sequenceNumber;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    /**
     * When a child-context is finished it reports its total sequence length,
     * such that offsets in this context can be converted to sequence numbers.
     */
    public void addChildSequenceLength(int childLength) {
        assert false;
    }

    /**
     * Returns the sequence of file locations spanning the given range. Assumes
     * that the range starts within this context.
     */
    public abstract boolean collectLocations(int sequenceNumber, int length, ArrayList<IASTNodeLocation> sofar);

    public int convertToSequenceEndNumber(int sequenceNumber) {
        // if the sequence number is the beginning of this context, skip the
        // denotation of this
        // context in the parent.
        if (sequenceNumber == fSequenceNumber) {
            return sequenceNumber - fEndOffsetInParent + fOffsetInParent;
        }
        return sequenceNumber;
    }

    /**
     * Returns the file location containing the specified offset range in this
     * context.
     */
    public ASTFileLocation createMappedFileLocation(int offset, int length) {
        return fParent.createMappedFileLocation(fOffsetInParent, fEndOffsetInParent - fOffsetInParent);
    }

    /**
     * Returns the macro-expansion surrounding or augmenting the given range, or
     * <code>null</code>.
     */
    public LocationCtxMacroExpansion findEnclosingMacroExpansion(int sequenceNumber, int length) {
        return null;
    }

    /**
     * Returns the minimal file location containing the specified sequence
     * number range, assuming that it is contained in this context.
     */
    public ASTFileLocation findMappedFileLocation(int sequenceNumber, int length) {
        return fParent.createMappedFileLocation(fOffsetInParent, fEndOffsetInParent - fOffsetInParent);
    }

    /**
     * Returns the minimal context containing the specified range, assuming that
     * it is contained in this context.
     */
    public LocationCtx findSurroundingContext(int sequenceNumber, int length) {
        return this;
    }

    public Collection<LocationCtx> getChildren() {
        return Collections.emptySet();
    }

    public String getFilePath() {
        return fParent.getFilePath();
    }

    /**
     * Support for the dependency tree, add inclusion statements found in this
     * context.
     */
    public void getInclusions(ArrayList<IASTInclusionNode> result) {
    }

    /**
     * Support for the dependency tree, returns inclusion statement that created
     * this context, or <code>null</code>.
     */
    public ASTInclusionStatement getInclusionStatement() {
        return null;
    }

    /**
     * Returns the line number for an offset within this context. Not all
     * contexts support line numbers, so this may return 0.
     */
    public int getLineNumber(int offset) {
        return 0;
    }

    final public ILocationCtx getParent() {
        return fParent;
    }

    /**
     * Returns the amount of sequence numbers occupied by this context including
     * its children.
     */
    public abstract int getSequenceLength();

    /**
     * Converts an offset within this context to the sequence number. In case
     * there are child-contexts behind the given offset, you need to set
     * checkChildren to <code>true</code>.
     */
    public int getSequenceNumberForOffset(int offset, boolean checkChildren) {
        return fSequenceNumber + offset;
    }

    public boolean isSourceFile() {
        if (fParent == null) {
            return false;
        }
        return fParent.isSourceFile();
    }
}