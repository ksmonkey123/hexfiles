package ch.awae.binfiles;

import org.jetbrains.annotations.NotNull;

import java.util.*;

class BinaryFileIterator implements Iterator<DataFragment> {

    private final @NotNull BinaryFile file;
    private final int stepSize;
    // queue to hold "remaining" elements of a slice
    private final Queue<DataFragment> queue = new ArrayDeque<>();
    private int nextStart;

    public BinaryFileIterator(@NotNull BinaryFile file, int stepSize) {
        this.file = file;
        this.stepSize = stepSize;
        this.fetchNextSegment();
    }

    @Override
    public boolean hasNext() {
        return !this.queue.isEmpty();
    }

    @Override
    public DataFragment next() {
        DataFragment fragment = this.queue.poll();
        if (fragment == null) {
            throw new NoSuchElementException();
        }
        if (queue.isEmpty()) {
            // we took the last element out of the queue, re-fill it!
            fetchNextSegment();
        }
        return fragment;
    }

    /**
     * searches for the next segment and puts it into the queue.
     * If we reach the end of the file without finding a segment, we return without adding
     * anything into the queue!
     */
    private void fetchNextSegment() {
        while (queue.isEmpty() && nextStart < file.getCurrentSize()) {
            // cap size so we don't run over the end of the file.
            int size = Math.min(stepSize, file.getSizeLimit() - nextStart);
            List<@NotNull DataFragment> fragments = file.getFragments(nextStart, stepSize);
            queue.addAll(fragments);
            nextStart = nextStart + stepSize;
        }
    }

}
