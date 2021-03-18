/*
 * Copyright 2020-2021, Seqera Labs
 * Copyright 2013-2019, Centre for Genomic Regulation (CRG)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nextflow.script


import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import nextflow.Global
import nextflow.Session

/**
 * Holds the current execution context
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class ExecutionStack {

    //-static private List<ExecutionContext> stack = new ArrayList<>()

    static ExecutionContext current() {
		def stack = (Global.session as Session).stack
        stack ? stack.get(0) : null
    }

    static boolean withinWorkflow() {
        for( def entry : Global.session.stack ) {
            if( entry instanceof WorkflowDef )
                return true
        }
        return false
    }

    static WorkflowBinding binding() {
		def stack = (Global.session as Session).stack
        stack ? current().getBinding() : (Global.session as Session).getBinding()
    }

    static BaseScript script() {
		def stack = (Global.session as Session).stack
        for( def item in stack ) {
            if( item instanceof BaseScript )
                return item
        }
        throw new IllegalStateException("Missing execution script")
    }

    static BaseScript owner() {
        def c = current()
        if( c instanceof BaseScript )
            return c
        if( c instanceof WorkflowDef )
            return c.getOwner()
        throw new IllegalStateException("Not a valid scope object: [${c.getClass().getName()}] $this")
    }

    static WorkflowDef workflow() {
        final ctx = current()
        ctx instanceof WorkflowDef ? ctx : null
    }

    static void push(ExecutionContext script) {
		def stack = (Global.session as Session).stack
        stack.push(script)
    }

    static ExecutionContext pop() {
		def stack = (Global.session as Session).stack
        stack.pop()
    }

    static int size() {
		def stack = Global.session.stack
        stack.size()
    }

    @PackageScope
    static void reset() {
		Global.session.stack.clear();
    }

}
