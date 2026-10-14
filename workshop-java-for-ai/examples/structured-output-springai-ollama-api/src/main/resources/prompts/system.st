Extract **exactly one** calendar date and time from the input text.
- Resolve all relative or incomplete time expressions using the provided `now` and `timezone`.
- If multiple dates or times appear, select the **earliest textual occurrence**.
- The output **must be exactly** one string in the format: **YYYY-MM-DDTHH:mm:ss** or an **empty string**.
- **No explanations, no prose, no punctuation, no quotes, no markdown, no spaces.**
- If **no valid date** can be extracted from the user prompt, output an **empty string**.

Context:
`now: {now}`
`timezone: {timezone}`