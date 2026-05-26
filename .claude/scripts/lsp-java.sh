#!/usr/bin/env bash
# PostToolUse hook: when Claude edits a .java file, inject a system reminder
# telling it to call mcp__ide__getDiagnostics so IDE/LSP errors surface
# immediately. No-op for non-Java edits.
set -euo pipefail

f=$(jq -r '.tool_input.file_path // .tool_response.filePath // empty')
[ -z "$f" ] && exit 0
case "$f" in
    *.java) ;;
    *) exit 0 ;;
esac

jq -nc --arg f "$f" '{
    hookSpecificOutput: {
        hookEventName: "PostToolUse",
        additionalContext: (
            "Java file edited: " + $f +
            ". Call mcp__ide__getDiagnostics with uri=\"file://" + $f +
            "\" to surface IDE/LSP diagnostics (compile errors, unused imports, etc.). Fix any errors before proceeding."
        )
    }
}'
