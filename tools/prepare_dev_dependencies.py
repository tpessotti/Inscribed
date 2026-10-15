"""Prepare local compile/audit dependencies without redistributing third-party JARs."""
from pathlib import Path
import argparse,shutil,zipfile
parser=argparse.ArgumentParser(description=__doc__)
for name in ['jei','lsbc','create']:parser.add_argument('--'+name,type=Path,required=True)
args=parser.parse_args();root=Path(__file__).resolve().parents[1];libs=root/'libs';libs.mkdir(exist_ok=True)
for name,filename in [('jei','jei-1.21.1-neoforge-19.57.0.451.jar'),('lsbc','lsbc-core-0.22.1.jar'),('create','create-1.21.1-6.0.10.jar')]:
    source=getattr(args,name).resolve();assert source.is_file(),f'Missing {name} JAR'
    destination=libs/filename
    if source!=destination.resolve():shutil.copy2(source,destination)
with zipfile.ZipFile(libs/'create-1.21.1-6.0.10.jar') as archive:
    candidates=[n for n in archive.namelist() if n.endswith('.jar') and 'ponder' in n.lower()]
    assert len(candidates)==1,f'Expected one nested Ponder JAR, found {candidates}'
    (libs/'ponder-neoforge-1.0.82+mc1.21.1.jar').write_bytes(archive.read(candidates[0]))
print('Development dependencies prepared in ignored libs/. No JAR is added to version control.')
