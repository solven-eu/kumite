import { ref, computed } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import { useRouter } from "vue-router";

import KumiteSearchById from "./kumite-search-by_id.js";

export default {
	components: {
		KumiteSearchById,
	},
	props: {
		someId: {
			type: String,
			// a UUID may be provided (by queryParams)
			required: false,
		},
	},
	computed: {
		...mapState(useKumiteStore, ["isLoggedIn"]),
	},
	setup(props) {
		const store = useKumiteStore();
		const router = useRouter();

		const idValidUuid = function (id) {
			// https://stackoverflow.com/questions/7905929/how-to-test-valid-uuid-guid
			// const uuidRegex =             /^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
			// We go with a simpler regex for now, as randomPlayer and fakePlayer has seemingly invalid UUIDs
			const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

			return uuidRegex.test(id);
		};

		const searchedId = ref(props.someId || "");
		const searchedIsValidUuid = computed(() => {
			return idValidUuid(searchedId.value);
		});

		const doSearch = function () {
			if (!searchedIsValidUuid.value) {
				console.warn("Interrupt doSearch given the id is invalid", searchedId.value);
				return;
			}

			router.push({ name: "search", params: { someId: searchedId.value } });
		};

		return { searchedId, searchedIsValidUuid, doSearch, idValidUuid };
	},
	template: /* HTML */ `
        <div v-if="!isLoggedIn">You need to login</div>
        <div v-else>
            <form>
                <div class="input-group mb-3">
                    <span class="input-group-text" id="searchedId">UUID</span>
                    <input
                        type="text"
                        class="form-control"
                        placeholder="some UUID"
                        aria-label="Username"
                        aria-describedby="searchedId"
                        v-model="searchedId"
                        required
                    />
                    <button class="btn btn-primary" type="button" @click="doSearch">Search</button>
                </div>
                <div id="searchedIdIsInvalid" v-if="!searchedIsValidUuid">Please provide a valid UUID.</div>
            </form>

            someId={{someId}}
            <KumiteSearchById :someId="someId" v-if="idValidUuid(someId)" />
        </div>
    `,
};
