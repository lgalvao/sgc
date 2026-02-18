import { describe, it, expect } from 'vitest';
import fc from 'fast-check';
import { mount } from '@vue/test-utils';
import ArvoreUnidades from '../unidade/ArvoreUnidades.vue';
import type { Unidade } from '@/types/tipos';

// Helper to generate a tree of units with unique IDs
const generateTree = (depth: number, breadth: number, startId = 1): Unidade[] => {
  if (depth === 0) return [];

  const units: Unidade[] = [];
  for (let i = 0; i < breadth; i++) {
    // Generate unique IDs by using position in tree
    // Ideally we'd use a counter, but for pure function we pass startId
    // Let's use a simpler approach: global counter in test or simple math
    // But generating a small tree is fine.

    // Depth 3, Breadth 2
    // Level 1: 1, 2
    // Level 2 (under 1): 11, 12. (under 2): 21, 22
    // Level 3 (under 11): 111, 112...

    const id = startId * 10 + i + 1;
    const children = generateTree(depth - 1, breadth, id);

    units.push({
      codigo: id,
      sigla: `U${id}`,
      nome: `Unidade ${id}`,
      isElegivel: true,
      filhas: children.length > 0 ? children : undefined,
      tipo: 'OPERACIONAL',
      unidadeSuperiorCodigo: Math.floor(startId / 10) // Approx
    });
  }
  return units;
};

// Flatten tree to list
const flatten = (nodes: Unidade[]): Unidade[] => {
    const list: Unidade[] = [];
    nodes.forEach(n => {
        list.push(n);
        if (n.filhas) list.push(...flatten(n.filhas));
    });
    return list;
};

describe('ArvoreUnidades Property-Based Tests', () => {

  it('should satisfy Monotonicity: Selecting a parent selects all eligible children', () => {
    // We generate a fixed structure but vary the interactions
    const tree = generateTree(3, 2, 0);
    const allNodes = flatten(tree);

    const wrapper = mount(ArvoreUnidades, {
      props: {
        unidades: tree,
        modelValue: []
      }
    });

    const vm = wrapper.vm as any;

    fc.assert(
      fc.property(fc.nat({ max: allNodes.length - 1 }), (nodeIndex) => {
          // Reset selection
          vm.deselecionarTodas();

          const targetNode = allNodes[nodeIndex];

          // Act: Toggle On
          vm.toggle(targetNode, true);

          // Assert: All eligible children must be selected (and the node itself)
          expect(vm.isChecked(targetNode.codigo)).toBe(true);

          const checkChildren = (node: Unidade) => {
              if (node.filhas) {
                  node.filhas.forEach(child => {
                      if (child.isElegivel) {
                          expect(vm.isChecked(child.codigo)).toBe(true);
                      }
                      checkChildren(child);
                  });
              }
          };
          checkChildren(targetNode);
      })
    );
  });

  it('should satisfy Selection State Invariant: Parent is checked iff all eligible children are checked', () => {
      const tree = generateTree(3, 2, 0);
      const allNodes = flatten(tree);

      const wrapper = mount(ArvoreUnidades, {
        props: {
            unidades: tree,
            modelValue: []
        }
      });
      const vm = wrapper.vm as any;

      fc.assert(
        fc.property(fc.array(fc.nat({ max: allNodes.length - 1 })), (indicesToSelect) => {
            // Reset
            vm.deselecionarTodas();

            // Select random nodes
            indicesToSelect.forEach(idx => {
                const node = allNodes[idx];
                vm.toggle(node, true);
            });

            // Verify Invariant for all parents
            allNodes.forEach(node => {
                if (node.filhas && node.filhas.length > 0) {
                    const allEligibleChildren = flatten(node.filhas).filter(n => n.isElegivel);
                    if (allEligibleChildren.length === 0) return; // Leaf effectively

                    const childrenDirect = node.filhas;
                    // The invariant "Parent is CHECKED iff ALL eligible children are CHECKED"
                    // usually applies to immediate children in recursive definition,
                    // or recursively. The component implements recursive logic.

                    // Let's check immediate children logic which is simpler and usually what "checkbox tree" means.
                    const allChildrenChecked = childrenDirect.every(child => {
                         if (!child.isElegivel) return true; // Ignored or treated as checked?
                         // Component logic:
                         // if (allChildrenSelected) { ... selectionSet.add(parent.codigo) ... }
                         // allChildrenSelected = children.every(child => selectionSet.has(child.codigo))

                         // So it requires ALL children (eligible or not? implementation checks 'every' child in 'children')
                         // But 'toggle' only adds 'isElegivel' children.
                         // So if a child is NOT eligible, it won't be in selectionSet.
                         // Thus 'allChildrenSelected' will be false if there is an ineligible child?
                         // Let's look at component:
                         // const allChildrenSelected = children.every(child => selectionSet.has(child.codigo));

                         // If there is an ineligible child, it can never be selected?
                         // If so, parent can never be selected?
                         // This might be a bug or feature.
                         return vm.isChecked(child.codigo);
                    });

                    if (allChildrenChecked && node.isElegivel) {
                        expect(vm.isChecked(node.codigo)).toBe(true);
                    } else {
                        // If not all children checked, parent should NOT be checked (unless it was selected individually and logic allows partial?)
                        // Tree usually enforces parent status based on children.
                        // But implementation says:
                        // } else if (parent.tipo !== 'INTEROPERACIONAL') { selectionSet.delete(parent.codigo); }
                        // So if OPERACIONAL, it is unchecked.
                        if (node.tipo !== 'INTEROPERACIONAL') {
                            expect(vm.isChecked(node.codigo)).toBe(false);
                        }
                    }
                }
            });
        })
      );
  });

  it('should satisfy Eligibility Invariant: Only eligible units can be selected', () => {
    // Generate base structure
    const treeStructure = generateTree(3, 2, 0); // ~14 nodes
    const allNodesStructure = flatten(treeStructure);

    // We need as many random properties as nodes
    const numNodes = allNodesStructure.length;

    fc.assert(
      fc.property(
        fc.array(fc.record({
          isElegivel: fc.boolean(),
          tipo: fc.constantFrom('OPERACIONAL', 'INTERMEDIARIA', 'INTEROPERACIONAL')
        }), { minLength: numNodes, maxLength: numNodes }),
        fc.array(fc.nat({ max: numNodes - 1 })), // User interactions (toggles)
        (randomProps, indicesToToggle) => {
          // Clone tree structure to avoid mutation across runs
          const tree = JSON.parse(JSON.stringify(treeStructure));
          const allNodes = flatten(tree);

          // Apply random properties
          allNodes.forEach((node, i) => {
             const props = randomProps[i];
             node.tipo = props.tipo as any;
             node.isElegivel = props.isElegivel;

             // Enforce domain rule: INTERMEDIARIA is never eligible
             if (node.tipo === 'INTERMEDIARIA') {
                 node.isElegivel = false;
             }
          });

          const wrapper = mount(ArvoreUnidades, {
            props: {
              unidades: tree,
              modelValue: []
            }
          });
          const vm = wrapper.vm as any;

          // Perform interactions
          indicesToToggle.forEach(idx => {
             const node = allNodes[idx];
             // Toggle state (simulate check/uncheck)
             const isSelected = vm.isChecked(node.codigo);
             vm.toggle(node, !isSelected);
          });

          // Assert Invariant: All selected units must be eligible
          const selectedNodes = allNodes.filter(n => vm.isChecked(n.codigo));

          selectedNodes.forEach(node => {
              expect(node.isElegivel).toBe(true);
              // Implicitly checks INTERMEDIARIA is not selected because we forced isElegivel=false for them.
              expect(node.tipo).not.toBe('INTERMEDIARIA');
          });
        }
      )
    );
  });

  it('should satisfy Idempotence: Selecting an already selected unit (or tree) should result in the same state', () => {
    fc.assert(
      fc.property(fc.nat({ max: 100 }), (seed) => { // Using seed to generate deterministic tree
         // Generate tree
         const tree = generateTree(3, 2, seed);
         const allNodes = flatten(tree);
         if (allNodes.length === 0) return;

         const wrapper = mount(ArvoreUnidades, {
            props: {
              unidades: tree,
              modelValue: []
            }
         });
         const vm = wrapper.vm as any;

         // Pick a random node
         const nodeIndex = seed % allNodes.length;
         const node = allNodes[nodeIndex];

         // Select it once
         vm.toggle(node, true);
         const stateAfterFirst = [...vm.modelValue].sort((a: number, b: number) => a - b);

         // Select it again
         vm.toggle(node, true);
         const stateAfterSecond = [...vm.modelValue].sort((a: number, b: number) => a - b);

         expect(stateAfterFirst).toEqual(stateAfterSecond);
      })
    );
  });
});
