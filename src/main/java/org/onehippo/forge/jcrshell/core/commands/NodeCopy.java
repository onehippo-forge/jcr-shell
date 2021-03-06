/*
 *  Copyright 2008-2018 Hippo.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.forge.jcrshell.core.commands;

import java.util.EnumSet;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.core.JcrShellPrinter;
import org.onehippo.forge.jcrshell.core.JcrWrapper;

/**
 * Copy a child node.
 */
public class NodeCopy extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.NODE,
            new ArgumentType(EnumSet.of(ArgumentType.Flags.NODE, ArgumentType.Flags.WRITE)) };

    public NodeCopy() {
        super("nodecopy", new String[] { "nodecp", "copynode", "cp" }, "nodecopy <nodename> <target parent>/[name]",
                "nodecp node target: copy node to target\n  If no name is specified, the current name is used.",
                ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        Node node = JcrWrapper.getCurrentNode();

        String src = args[1];
        String dest = args[2];

        // tab completion slash
        if (src.endsWith("/")) {
            src = src.substring(0, src.length() - 1);
        }

        Node srcNode;
        if (src.startsWith("/")) {
            if (!node.getSession().getRootNode().hasNode(src.substring(1))) {
                JcrShellPrinter.println("Src node not found: " + src);
                return false;
            }
            srcNode = node.getSession().getRootNode().getNode(src.substring(1));
        } else {
            if (!node.hasNode(src)) {
                JcrShellPrinter.println("Src node not found: " + src);
                return false;
            }
            srcNode = node.getNode(src);
        }
        if (!dest.startsWith("/")) {
            if (node.getPath().equals("/")) {
                dest = "/" + dest;
            } else {
                dest = node.getPath() + "/" + dest;
            }
        }
        if (dest.endsWith("/")) {
            dest = dest + srcNode.getName();
        }
        try {
            JcrWrapper.copyNode(srcNode, dest);
        } catch (ItemExistsException e) {
            JcrShellPrinter.printWarnln("Target already exists: " + dest);
        } catch (PathNotFoundException e) {
            JcrShellPrinter.printWarnln("Target not found: " + dest);
        }
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 3;
    }
}
