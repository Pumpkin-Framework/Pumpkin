package nl.jk5.pumpkin.server.scripting.component.impl.fs;

import com.google.common.base.MoreObjects;
import nl.jk5.pumpkin.server.scripting.AbstractValue;
import nl.jk5.pumpkin.server.scripting.Context;

public final class HandleValue implements AbstractValue {

    private final String address;
    private final int handle;

    public HandleValue(String address, int handle) {
        this.address = address;
        this.handle = handle;
    }

    @Override
    public void dispose(Context context) {
        /*
        TODO
        if (context.node() != null && context.node().network() != null) {
            val node = context.node().network().node(owner)
            if (node != null) {
                node.host() match {
                    case fs: FileSystem => try fs.close(context, handle) catch {
                        case _: Throwable => // Ignore, already closed.
                    }
                }
            }
        }
         */
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("Handle")
                .add("handle", handle)
                .toString();
    }

    public int getHandle() {
        return handle;
    }
}
