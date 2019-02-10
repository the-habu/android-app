https://mermaidjs.github.io/mermaid-live-editor/#/edit/eyJjb2RlIjoiZ3JhcGggTFJcblxuQVtTdGFydCBBUFBdXG5BLS0-IEZbRW50ZXIgTmFtZV1cbkYgLS0-QltQYWlyIFRhZ2dlcl1cblxuQiAtLT5De1BsYXkgTW9kZX1cbkMgLS0-R1tTaG9vdF0gXG5cbkMgLS0-RVtHZXQgaGl0XVxuRS0tPkhbVGltZW91dF0gXG5cbkggLS0-IENcblxuIiwibWVybWFpZCI6eyJ0aGVtZSI6ImRlZmF1bHQifX0

graph LR

A[Start APP]
A--> F[Enter Name]
F -->B[Pair Tagger]

B -->C{Play Mode}
C -->G[Shoot] 

C -->E[Get hit]
E-->H[Timeout] 

H --> C