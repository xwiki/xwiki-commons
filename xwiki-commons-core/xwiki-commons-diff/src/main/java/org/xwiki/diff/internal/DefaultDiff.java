package org.xwiki.diff.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.xwiki.diff.ChangeDelta;
import org.xwiki.diff.Chunk;
import org.xwiki.diff.DeleteDelta;
import org.xwiki.diff.Delta;
import org.xwiki.diff.Diff;
import org.xwiki.diff.DiffConfiguration;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.InsertDelta;
import org.xwiki.diff.MergeConfiguration;
import org.xwiki.diff.MergeException;
import org.xwiki.diff.MergeResult;
import org.xwiki.diff.Patch;

import difflib.DiffUtils;

public class DefaultDiff implements Diff
{
    @Override
    public <E> Patch<E> diff(List<E> previous, List<E> next, DiffConfiguration<E> diff) throws DiffException
    {
        return toPatch(DiffUtils.diff(previous, next));
    }

    @Override
    public <E> MergeResult<E> merge(List<E> commonAncestor, List<E> next, List<E> current,
        MergeConfiguration<E> configuration) throws MergeException
    {
        MergeResult<E> mergeResult = new MergeResult<E>(commonAncestor, next, current);

        if (ObjectUtils.equals(commonAncestor, next)) {
            // No change so nothing to do
            return mergeResult;
        }

        if (current.isEmpty()) {
            if (ObjectUtils.equals(commonAncestor, next)) {
                // No change so nothing to do
                return mergeResult;
            } else {
                // The current version has been replaced by an empty string
                mergeResult.getErrors().add(new MergeException("The current value has been replaced by empty string"));
                resultStr = current;
            }
        } else {
            if (current.get(0) instanceof String) {
                merge(toString((List<String>) commonAncestor), toString((List<String>) next),
                    toString((List<String>) current), configuration, mergeResult);
            } else {
                
            }
        }
    }

    public <E> void merge(String commonAncestor, String next, String current, MergeConfiguration<E> configuration,
        MergeResult<E> mergeResult) throws MergeException
    {
        String resultStr;

        com.qarks.util.files.diff.MergeResult result =
            com.qarks.util.files.diff.Diff.quickMerge(commonAncestor, next, current, false);

        if (result.isConflict()) {
            mergeResult.getErrors().add(
                new MergeException(String.format(
                    "Failed to merge with previous string [%s], new string [%s] and current string [%s]",
                    commonAncestor, next, current)));
            resultStr = current;
        } else {
            resultStr = result.getDefaultMergedResult();
            mergeResult.setModified(true);
        }

        return resultStr;
    }

    private String toString(List<String> list)
    {
        return StringUtils.join(list, '\n');
    }

    private List<String> toLines(String str)
    {
        try {
            return IOUtils.readLines(new StringReader(str));
        } catch (IOException e) {
            // Should never happen
            return null;
        }
    }

    private <E> Patch<E> toPatch(difflib.Patch patch) throws DiffException
    {
        Patch<E> newPatch = new Patch<E>();

        for (difflib.Delta delta : patch.getDeltas()) {
            newPatch.add(this.<E> toDelta(delta));
        }

        return newPatch;
    }

    private <E> Delta<E> toDelta(difflib.Delta delta) throws DiffException
    {
        Delta<E> newDelta;

        switch (delta.getType()) {
            case CHANGE:
                newDelta =
                    new ChangeDelta<E>(this.<E> toChunk(delta.getOriginal()), this.<E> toChunk(delta.getRevised()));
                break;
            case DELETE:
                newDelta =
                    new DeleteDelta<E>(this.<E> toChunk(delta.getOriginal()), this.<E> toChunk(delta.getRevised()));
                break;
            case INSERT:
                newDelta =
                    new InsertDelta<E>(this.<E> toChunk(delta.getOriginal()), this.<E> toChunk(delta.getRevised()));
                break;
            default:
                throw new DiffException(String.format("Failed to convert [%s] info [%s]. Unknown type [%s]", delta
                    .getClass().getName(), Delta.class.getName(), delta.getType().toString()));
        }

        return newDelta;
    }

    private <E> Chunk<E> toChunk(difflib.Chunk chunk)
    {
        return new Chunk<E>(chunk.getPosition(), (List<E>) chunk.getLines());
    }
}
