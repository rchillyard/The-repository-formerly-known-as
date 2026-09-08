import re,sys
s=open(sys.argv[1]).read()
s=s.replace('\\documentclass[acmtog]{acmart}', """%% Preamble per SIAM's example_doublecolumn.tex (022425).
\\documentclass[twoside,leqno,twocolumn]{article}
\\usepackage[letterpaper]{geometry}
\\makeatletter
\\@ifundefined{AddToHook}{\\newcommand{\\AddToHook}[2]{}}{}
\\makeatother
\\usepackage{siamproceedings}
\\usepackage[T1]{fontenc}
\\usepackage{amsfonts}
\\usepackage{epstopdf}
\\usepackage{enumitem}
\\usepackage{xcolor}
\\usepackage{booktabs}
\\usepackage{tabularx}
\\usepackage{comment}""")
for cmd in ['setcopyright','copyrightyear','acmYear','acmDOI','settopmatter','acmJournal',
            'acmVolume','acmNumber','acmArticle','acmMonth','keywords','citestyle','acmSubmissionID']:
    s=re.sub(r'^\\'+cmd+r'\{[^}]*\}\s*$','',s,flags=re.M)
s=re.sub(r'^\\ccsdesc(\[[^\]]*\])?\{[^}]*\}\s*$','',s,flags=re.M)
s=s.replace('\\renewcommand\\footnotetextcopyrightpermission[1]{}','')
s=re.sub(r'\\begin\{CCSXML\}.*?\\end\{CCSXML\}','',s,flags=re.S)
for pat in [r'\\author\{[^}]*\}', r'\\email\{[^}]*\}', r'\\orcid\{[^}]*\}']:
    s=re.sub(pat,'',s)
s=re.sub(r'\\authornote\{.*?\n\s*\}','',s,flags=re.S)
s=re.sub(r'\\affiliation\{.*?\n\}','',s,flags=re.S)
s=re.sub(r'\\begin\{acks\}(.*?)\\end\{acks\}',r'\\section*{Acknowledgments}\1',s,flags=re.S)
s=re.sub(r'\\Description\{.*?\}\s*','',s,flags=re.S)
s=s.replace('\\bibliographystyle{ACM-Reference-Format}','\\bibliographystyle{siamplain}')
s=s.replace('\\usepackage[boxruled, linesnumbered, noend, noline]{algorithm2e}',
            '\\usepackage[algo2e, boxruled, linesnumbered, noend, noline]{algorithm2e}')
s=s.replace('\\begin{algorithm}','\\begin{algorithm2e}').replace('\\end{algorithm}','\\end{algorithm2e}')
i=s.find('% Code for ORCID iD'); j=s.find('\\end{comment}', i)
if i>0 and j>i: s=s[:i]+'%% ORCID block removed (anonymous submission)\n'+s[j+len('\\end{comment}'):]
s=re.sub(r'\\ifdraftstamp\s*\n\s*\\makeatletter.*?\\fi','',s,flags=re.S)
open(sys.argv[2],'w').write(s)
