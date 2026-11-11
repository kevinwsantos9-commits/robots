# Cyber Parts (Fabric 1.21.1)

Troque partes do corpo por peças robóticas na **Bancada Cibernética**.

## Compilar
1. Suba esta pasta para um repositório no GitHub.
2. Aba **Actions** -> workflow **build** -> baixe o artefato `cyberparts-jar`.
3. Coloque o `.jar` em `mods/` junto do Fabric API (Minecraft 1.21.1, Java 21).

## Como usar
Receita da bancada: 3 barras de ferro (topo), redstone + mesa de trabalho + redstone (meio), 3 blocos de ferro (base).
Clique direito na bancada -> menu -> clique numa parte para instalar (gasta materiais e dói). Clique de novo para remover.

Ajuste custos, dano e atributos em `Part.java`.

## Modelos 3D (v1.1)
As peças aparecem no corpo do jogador (visível para outros jogadores) e os braços também na primeira pessoa.
Os modelos ficam em `RobotModel.java`; a textura em `assets/cyberparts/textures/entity/robot.png` (zonas A=aço, B=juntas, C=ciano, D=vermelho).
`robot_glow.png` define o que brilha no escuro.

## v1.2 - Aba própria e 3 níveis por parte
- Aba criativa **Cyber Parts** com a bancada e 21 peças (7 partes x Mk1/Mk2/Mk3).
- Receitas: Mk1 = ferro + redstone + item base (capacete, peitoral, espada...). Mk2 = Mk1 + ouro + diamante + bloco de redstone. Mk3 = Mk2 + netherite + blocos de diamante.
- Na bancada: clique em Mk1/Mk2/Mk3 para instalar a peça do seu inventário (ela é consumida). Clique no nível instalado para remover (a peça volta). Clicar em outro nível troca.
- Mk2 = x1,6 de bônus, Mk3 = x2,4. Quanto maior o nível, maior o dano da cirurgia.
- Cada nível tem textura e detalhes de modelo próprios (aço/ciano, ouro/laranja, obsidiana/vermelho).
