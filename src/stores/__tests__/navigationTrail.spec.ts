import {beforeEach, describe, expect, it} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import type {TrailCrumb} from '../navigationTrail'; // Import TrailCrumb from the same file
import {useNavigationTrail} from '../navigationTrail';

describe('useNavigationTrail', () => {
    let navigationTrailStore: ReturnType<typeof useNavigationTrail>;

    beforeEach(() => {
        setActivePinia(createPinia());
        navigationTrailStore = useNavigationTrail();
        // Reset the store state before each test
        navigationTrailStore.$patch({
            crumbs: [] // Start with an empty array for consistent tests
        });
    });

    it('should initialize with an empty array of crumbs', () => {
        expect(navigationTrailStore.crumbs.length).toBe(0);
    });

    describe('actions', () => {
        it('reset should clear the crumbs array', () => {
            navigationTrailStore.crumbs = [{label: 'Home'}, {label: 'Page'}];
            navigationTrailStore.reset();
            expect(navigationTrailStore.crumbs.length).toBe(0);
        });

        it('set should set the crumbs array', () => {
            const newCrumbs: TrailCrumb[] = [{label: 'New Home'}, {label: 'New Page'}];
            navigationTrailStore.set(newCrumbs);
            expect(navigationTrailStore.crumbs).toEqual(newCrumbs);
        });

        it('push should add a crumb to the end of the array', () => {
            navigationTrailStore.crumbs = [{label: 'Home'}];
            const newCrumb: TrailCrumb = {label: 'New Crumb'};
            navigationTrailStore.push(newCrumb);
            expect(navigationTrailStore.crumbs.length).toBe(2);
            expect(navigationTrailStore.crumbs[1]).toEqual(newCrumb);
        });

        it('popTo should remove crumbs from the specified index to the end', () => {
            navigationTrailStore.crumbs = [{label: 'A'}, {label: 'B'}, {label: 'C'}, {label: 'D'}];
            navigationTrailStore.popTo(1); // Pop to index 1 (keep A, B)
            expect(navigationTrailStore.crumbs.length).toBe(2);
            expect(navigationTrailStore.crumbs).toEqual([{label: 'A'}, {label: 'B'}]);
        });

        it('popTo should not change state if index is out of bounds', () => {
            navigationTrailStore.crumbs = [{label: 'A'}, {label: 'B'}];
            const initialLength = navigationTrailStore.crumbs.length;
            navigationTrailStore.popTo(999); // Index too high
            expect(navigationTrailStore.crumbs.length).toBe(initialLength);
            navigationTrailStore.popTo(-1); // Index too low
            expect(navigationTrailStore.crumbs.length).toBe(initialLength);
        });

        it('ensureBase should add a default home crumb if the array is empty', () => {
            navigationTrailStore.crumbs = [];
            navigationTrailStore.ensureBase();
            expect(navigationTrailStore.crumbs.length).toBe(1);
            expect(navigationTrailStore.crumbs[0]).toEqual({label: '__home__', to: {path: '/painel'}, title: 'Painel'});
        });

        it('ensureBase should not add a home crumb if the array is not empty', () => {
            navigationTrailStore.crumbs = [{label: 'Existing'}];
            const initialLength = navigationTrailStore.crumbs.length;
            navigationTrailStore.ensureBase();
            expect(navigationTrailStore.crumbs.length).toBe(initialLength);
            expect(navigationTrailStore.crumbs[0]).toEqual({label: 'Existing'});
        });
    });
});
